package models;

import io.ebean.*;
import models.Record;
import oracle.sql.DATE;
import javax.persistence.*;

@Entity
@Table(name = "ES_DATATYPE_CONFIG")
public class DataType extends Model {
    @Column(name = "DATATYPE", nullable = true, length = 16, updatable = false, insertable = false)
    public String dataType = "";

    @Column(name = "BINTYPE", nullable = true, length = 16, updatable = false, insertable = false)
    public String binType = "";

    @Column(name = "FILENAMEPATTERN", nullable = true, length = 512, updatable = false, insertable = false)
    public String namePattern = "";

    @Column(name = "FILEINDEX", nullable = true, length = 8, updatable = false, insertable = false)
    public String fileIndex = "";

    @Column(name = "STORAGETYPE", nullable = true, length = 8, updatable = false, insertable = false)
    public String storageType = "";

    @Column(name = "FILEPROP", nullable = true, length = 8, updatable = false, insertable = false)
    public String fileProp = "";

    @Column(name = "EXCLUDE", nullable = true, length = 64, updatable = false, insertable = false)
    public String exclude = "";

    //database finder, used for query
    public static final Finder<Long, DataType> finder = new Finder(DataType.class);
}