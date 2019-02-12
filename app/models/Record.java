package models;

import io.ebean.*;
import javax.persistence.*;

@Entity
@Table(name = "ES_RECORD_2")
public class Record extends Model {
    @Id
    @Column(name = "RECORDID", nullable = false, unique = true, length = 64)
    public String ID = "";

    @Column(name = "RUNFOLDER", nullable = true, length = 256)
    public String runFolder = "";

    @Column(name = "LIBRARYID", nullable = true, length = 64)
    public String libID = "";

    @Column(name = "ACCESSIONID", nullable = true, length = 64)
    public String accID = "";

    @Column(name = "CAPTURESET", nullable = true, length = 64)
    public String captureSet = "";

    @Column(name = "TESTID", nullable = true, length = 64)
    public String testID = "";

    @Column(name = "PANELNAME", nullable = true, length = 256)
    public String panelName = "";

    @Column(name = "EXTID", nullable = true, length = 64)
    public String extID = "";

    @Column(name = "INFO", nullable = true, length = 64)
    public String info = "";

    @Column(name = "DATATYPE", nullable = true, length = 16)
    public String dataType = "";

    @Column(name="TOOLTYPE", nullable = true, length = 16)
    public String toolType = "";

    @Column(name="BAMFOLDER", nullable = true, length = 128)
    public String bamFolder = "";

    @Column(name="VERSION", nullable = true, length = 5)
    public String ver = "";

    @Column(name="DATECODE", nullable = false, length = 16)
    public String dateCode = "";

    @Column(name="SYS_IDX", nullable = false)
    public int sysID = 0;

    @Column(name="OP_USER", nullable = true, length = 16)
    public String opUser = "";

    @OneToOne(mappedBy = "record", fetch=FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    public RecordKey recordKey;

    //database finder, used for query
    public static final Finder<Long, Record> finder = new Finder(Record.class);

}
