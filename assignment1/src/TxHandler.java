import java.util.ArrayList;
import java.util.Arrays;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        java.util.HashSet<UTXO> used = new java.util.HashSet<UTXO>();
        double insum = 0;
        for (int index = 0; index < tx.numInputs(); ++index) {
            Transaction.Input in = tx.getInput(index);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            if (!pool.contains(utxo) || used.contains(utxo)) return false;
            used.add(utxo);
            Transaction.Output outTx = pool.getTxOutput(utxo);
            if (!Crypto.verifySignature(outTx.address, tx.getRawDataToSign(index), in.signature)) return false;
            insum += outTx.value;
        }
        double outsum = 0;
        for (Transaction.Output out : tx.getOutputs()) {
            if (out.value <= 0) return false;
            outsum += out.value;
        }
        if (insum < outsum) return false;
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> txs = new ArrayList<Transaction>(Arrays.asList(possibleTxs));
        // remove in-place
        txs.removeIf(tx -> !isValidTx(tx));
        for (Transaction tx : txs) {
            for (Transaction.Input txi : tx.getInputs()) {
                UTXO utxo = new UTXO(txi.prevTxHash, txi.outputIndex);
                pool.removeUTXO(utxo);
            }
        }
        return txs.toArray(new Transaction[]{});
    }

    private UTXOPool pool;
}
