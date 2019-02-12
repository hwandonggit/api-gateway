package models;

import io.ebean.*;
import javax.persistence.*;

@Entity
@Table(name = "ES_KEY_REFERENCE")
public class Config extends Model {
    @Column(name = "DATATYPE", nullable = true, length = 16, updatable = false, insertable = false)
    public String dataType = "";

    @Column(name = "BINTYPE", nullable = true, length = 16, updatable = false, insertable = false)
    public String binType = "";

    @Column(name = "FILENAMEPATTERN", nullable = true, length = 512, updatable = false, insertable = false)
    public int filenamePattern = 0;

    @Column(name = "MAXCAP", nullable = true, updatable = false, insertable = false)
    public int oriDataPath = 0;

    @Column(name = "SYS_IDX", nullable = true, updatable = false, insertable = false)
    public int idx = 0;

    //database finder, used for query
    public static final Finder<Long, Config> finder = new Finder(Config.class);
}
