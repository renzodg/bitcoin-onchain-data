package models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
public class BlockHeader {

    private String hash;
    private int confirmations;
    private long height;
    private long version;
    private String versionHex;
    @SerializedName("merkleroot")
    private String merkleRoot;
    private LocalDateTime time;
    @SerializedName("mediantime")
    private LocalDateTime medianTime;
    private long nonce;
    private String bits;
    private double difficulty;
    @SerializedName("chainwork")
    private String chainWork;
    private int nTx;
    @SerializedName("previousblockhash")
    private String previousBlockHash;
    @SerializedName("nextblockhash")
    private String nextBlockHash;


}
