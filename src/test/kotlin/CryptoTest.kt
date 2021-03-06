import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

object CryptoTest: Spek({
    given("Crypto Object")
    {
        on("Passphrase 'this is a top secret passphrase'")
        {
            val passphrase =  "this is a top secret passphrase"

            it("Should generate address 'AGeYmgbg2LgGxRW2vNNJvQ88PknEJsYizC' on Mainnet")
            {
                val address = Crypto.getAddress(Crypto.getKeys(passphrase))
                assertEquals("AGeYmgbg2LgGxRW2vNNJvQ88PknEJsYizC", address)
            }

            it("Should generate address 'D61mfSggzbvQgTUe6JhYKH2doHaqJ3Dyib' on Devnet")
            {
                Crypto.network = NetworkConstants.devnet
                val address = Crypto.getAddress(Crypto.getKeys(passphrase))
                assertEquals("D61mfSggzbvQgTUe6JhYKH2doHaqJ3Dyib", address)
            }
        }

        context("Transaction Creation")
        {
            val transactionNormal = Crypto.createTransaction("AXoXnFi4z1Z6aFvjEYkDVCtBGW2PaRiM25",
                    133380000000,
                    "This is first transaction from JAVA",
                    "this is a top secret passphrase")

            val transactionVote = Crypto.createVote(listOf("+034151a3ec46b5670a682b0a63394f863587d1bc97483b1b6c70eb58e7f0aed192"),
                    "this is a top secret passphrase")

            val transactionDelegate = Crypto.createDelegate("polopolo",
                    "this is a top secret passphrase")


            on("Single Signature")
            {
                it("should verify if normal transaction")
                {
                    assert(Crypto.verify(transactionNormal))
                }

                it("should verify if vote transaction")
                {
                    assert(Crypto.verify(transactionVote))
                }

                it("should verify if delegate transaction")
                {
                    assert(Crypto.verify(transactionDelegate))
                }

                it("should fail to verify if amount is modified")
                {
                    transactionNormal.amount = 100000000000000
                    assertFalse(Crypto.verify(transactionNormal))
                }

                it("should fail to verify if fee is modified")
                {
                    transactionNormal.fee = 11
                    assertFalse(Crypto.verify(transactionNormal))
                }

                it("should fail to verify if recipientId is modified")
                {
                    transactionNormal.recipientId = "AavdJLxqBnWqaFXWm2xNirNArJNUmyUpup"
                    assertFalse(Crypto.verify(transactionNormal))
                }

                it("should serialize/deserialize to JSON")
                {
                    assertEquals(transactionNormal, Crypto.fromJson(transactionNormal.toJson().toString()))
                }
            }

            on("Second Signature")
            {
                val secondPassphrase = "second passphrase"
                val secondSignTransaction = Crypto.createSecondSignature("first passphrase", secondPassphrase)

                it("should verify first signature")
                {
                    assert(Crypto.verify(secondSignTransaction))
                }

                it("should verify second signature")
                {
                    assert(Crypto.secondVerify(secondSignTransaction, secondSignTransaction.senderPublicKey!!))
                }

                it("should have null signSignature")
                {
                    assert(secondSignTransaction.signSignature == null)
                }

                it("should have the correct encode public key")
                {
                    assertEquals(Crypto.base16Encode(Crypto.getKeys(secondPassphrase)!!.pubKey),secondSignTransaction.asset.signature)
                }
            }
        }
    }

})
