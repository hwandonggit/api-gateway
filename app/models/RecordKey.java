package models;

import io.ebean.*;
import oracle.sql.DATE;
import javax.persistence.*;

@Entity
@Table(name = "ES_RECORD_KEY_2")
public class RecordKey extends Model {
    @Id
    @Column(name = "RECORDID", nullable = false, unique = true, length = 256)
    public String ID = "";

    @Column(name = "REFERENCEID", nullable = true, length = 32)
    public String referenceID = "";

    @Column(name = "BINTYPE", nullable = true, length = 16)
    public String binType = "";

    @Column(name = "FILENAME", nullable = true, length = 1024)
    public String fileName = "";

    @Column(name = "ORIDATAPATH", nullable = true, length = 256)
    public String oriDataPath = "";

    @Column(name = "ENROLLDATE", nullable = true)
    public DATE date = new DATE();

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RECORDID", insertable = false, updatable = false)
    public Record record;

    //database finder, used for query
    public static final Finder<Long, RecordKey> finder = new Finder(RecordKey.class);
}
