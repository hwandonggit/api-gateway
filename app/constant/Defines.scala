package constant

//=============================================================================
// Globally definitions
//=============================================================================
object DATASET_LEVEL {
  val RUN         = "RUN"
  val LIBRARY     = "LIBRARY"
  val ACCESSION   = "ACCESSION"
  val PANEL       = "PANEL"
}

/**
  * scala wrapper for the bioInfoPipeline input tool types
  */
object TOOLTYPE {
  val DRAGEN      = "DRAGEN"
  val NOVO        = "NOVO"
  val UNKNOWN     = "UNKNOWN"
}

/**
  * scala wrapper for the bioInfoPipeline input file types
  */
object DATATYPE {
  def getProc(dataType: String): String = {
    dataType match {
      case DATATYPE.ACCESSION.AUDIT
           | DATATYPE.ACCESSION.LOG
           | DATATYPE.ACCESSION.BAM
           | DATATYPE.ACCESSION.FASTQ
           | DATATYPE.ACCESSION.COVERAGE
           | DATATYPE.ACCESSION.PERM
           | DATATYPE.ACCESSION.SOMATIC.DNA.BAM
           | DATATYPE.ACCESSION.SOMATIC.DNA.CNV
           | DATATYPE.ACCESSION.SOMATIC.DNA.COV
           | DATATYPE.ACCESSION.SOMATIC.DNA.PERM
           | DATATYPE.ACCESSION.SOMATIC.DNA.SMV
           | DATATYPE.ACCESSION.SOMATIC.RNA.BAM
           | DATATYPE.ACCESSION.SOMATIC.RNA.FUSION
           | DATATYPE.ACCESSION.SOMATIC.RNA.PERM
           | DATATYPE.ACCESSION.SOMATIC.RNA.DEL
           | DATATYPE.ACCESSION.TYPE => "accession"
      case DATATYPE.PANEL.FV
           | DATATYPE.PANEL.AUDIT
           | DATATYPE.PANEL.LOG
           | DATATYPE.PANEL.CNVPLUS
           | DATATYPE.PANEL.CNV3
           | DATATYPE.PANEL.COVERAGE
           | DATATYPE.PANEL.RESULT
           | DATATYPE.PANEL.TYPE
           | DATATYPE.PANEL.SOMATIC.TMB
           | DATATYPE.PANEL.SOMATIC.REPORT => "panel"
      case DATATYPE.LIBRARY.AUDIT
           | DATATYPE.LIBRARY.LOG
           | DATATYPE.LIBRARY.TYPE => "library"
      case DATATYPE.RUN.AUDIT
           | DATATYPE.RUN.LOG
           | DATATYPE.RUN.ARCHIVE
           | DATATYPE.RUN.ARCHIVE_SAS
           | DATATYPE.RUN.CACHE
           | DATATYPE.RUN.CACHE_LIB
           | DATATYPE.RUN.TYPE => "run"
    }
  }

  //accession ------- sample
  object ACCESSION {
    val TYPE      = "ACCESSION"
    val BAM       = "BAM"
    val FASTQ     = "FASTQ"
    val COVERAGE  = "ACC_COVERAGE"
    val PERM      = "ACC_PERM"
    val LOG       = "ACC_LOG"
    val AUDIT     = "ACC_AUDIT"

    object SOMATIC {
      object DNA {
        val BAM               = "ACC_SOM_DNA_BAM"
        val BAI               = "ACC_SOM_DNA_BAI"
        val COV               = "ACC_SOM_DNA_COV"
        val PERM              = "ACC_SOM_DNA_PERM"
        val CNV               = "ACC_SOM_DNA_CNV"
        val SMV               = "ACC_SOM_DNA_SMV"
      }
      object RNA {
        val BAM               = "ACC_SOM_RNA_BAM"
        val BAI               = "ACC_SOM_RNA_BAI"
        val PERM              = "ACC_SOM_RNA_PERM"
        val FUSION            = "ACC_SOM_RNA_FS"
        val DEL               = "ACC_SOM_RNA_DEL"
      }
    }
  }

  //panel test -------
  object PANEL {
    val TYPE      = "PANEL"
    val RESULT    = "PANEL"
    val COVERAGE  = "PANEL_COVERAGE"
    val FV        = "FV"
    val CNV3      = "CNV3"
    val CNVPLUS   = "CNVPLUS"
    val AUDIT     = "PANEL_AUDIT"
    val LOG       = "PANEL_LOG"

    object SOMATIC {
      val REPORT  = "PANEL_SOM_REPORT"
      val TMB     = "PANEL_SOM_TMB"
    }
  }

  // run
  object RUN {
    val TYPE          = "RUN"
    val ARCHIVE       = "RUN_ARCHIVE"
    val ARCHIVE_SAS   = "RUN_ARCHIVE_SAS"
    val AUDIT         = "RUN_AUDIT"
    val LOG           = "RUN_LOG"
    val CACHE         = "RUN_CACHE_SAMPLE"
    val CACHE_LIB     = "RUN_CACHE_SAMPLE_LIB"
  }

  // lib
  object LIBRARY {
    val TYPE      = "LIBRARY"
    val AUDIT     = "LIB_AUDIT"
    val LOG       = "LIB_LOG"
  }

}

object ES_DB {

  object RECORDID {
    val PREFIX    = "ES"
  }

}