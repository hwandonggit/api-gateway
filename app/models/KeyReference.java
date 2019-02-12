package models;

        import io.ebean.Finder;
        import io.ebean.Model;

        import javax.persistence.Column;
        import javax.persistence.Entity;
        import javax.persistence.Id;
        import javax.persistence.Table;

@Entity
@Table(name = "ES_KEY_REFERENCE_2")
public class KeyReference extends Model {
    @Id
    @Column(name = "REFERENCEID", nullable = false, length = 32)
    public String ID = "";

    @Column(name = "PARENTPATH", nullable = true, length = 256)
    public String parentPath = "";

    @Column(name = "BINTYPE", nullable = true, length = 16)
    public String binType = "";

    @Column(name = "CURCAP", nullable = true)
    public long currentCapacity = 0;

    @Column(name = "MAXCAP", nullable = true)
    public long maxCapacity = 0;

    @Column(name = "SYS_IDX", nullable = true)
    public int idx = 0;

    //database finder, used for query
    public static final Finder<Long, KeyReference> finder = new Finder(KeyReference.class);
}
