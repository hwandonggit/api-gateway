package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "PLM_TXS")
public class PLMTransaction extends Model {
    @Id
    @Column(name = "ID", nullable = false, length = 254)
    public String ID = "";

    @Column(name = "PARENT_DIR", nullable = true, length = 254)
    public String parentPath = "";

    @Column(name = "RUN_DIR", nullable = true, length = 254)
    public String runFolder = "";

    @Column(name = "STATUS", nullable = false, length = 254)
    public String status = "";

    @Column(name = "CREATED", nullable = false)
    public String createdTimeStamp = "";

    @Column(name = "MODIFIED", nullable = false)
    public String modifiedTimeStamp = "";

    @Column(name = "WORKFLOW", nullable = false)
    public String workflow = "";

    @Transient
    public Boolean splited = false;

    @Transient
    public Boolean isArchiving = false;

    @Transient
    public Boolean enableArchiving = false;

    @Transient
    public Boolean archived = false;

    @Transient
    public Boolean existed = false;

    public Date created() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date();
        try {
            date = df.parse(createdTimeStamp);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

    public Date modified() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date();
        try {
            date = df.parse(modifiedTimeStamp);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

    //database finder, used for query
    public static final Finder<Long, PLMTransaction> finder = new Finder(PLMTransaction.class);

}
