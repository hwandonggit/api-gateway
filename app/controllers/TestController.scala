package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._

class TestController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  //To enroll accession data: {type: BAM/ACC_Audit}
  def test = Action {
    Ok(Json.obj(
      "result" -> "SUCCESS",
      "message" -> "",
      "keyValueTable" -> Json.arr(),
      "source" -> "ftflw02",
      "runobject" -> Json.obj(
        "laneconfiguration" -> Json.obj(
          "1" -> Json.arr("libDNA", "libRNA"),
          "2" -> Json.arr("libDNA", "libRNA"),
          "3" -> Json.arr("libDNA", "libRNA"),
          "4" -> Json.arr("libDNA", "libRNA"),
          "5" -> Json.arr(),
          "6" -> Json.arr(),
          "7" -> Json.arr(),
          "8" -> Json.arr(),
        ),
        "librarydefinitions" -> Json.obj(
          "libDNA" -> Json.obj(
            "samples" -> Json.arr(
              Json.obj(
                "accessionid" -> "FT-SOM9",
                "tag" -> "",
                "barcode" -> "CGGCTATG",
                "endbarcode" -> "GTACTGAC", //GTCAGTAC
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM10",
                "tag" -> "",
                "barcode" -> "TCTCGCGC",
                "endbarcode" -> "GTACTGAC", //GTCAGTAC
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM11",
                "tag" -> "",
                "barcode" -> "CATCGAGG",
                "endbarcode" -> "GGCGACGG", //CCGTCGCC
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM15",
                "tag" -> "",
                "barcode" -> "CTCGACTG",
                "endbarcode" -> "GGCGACGG", //CCGTCGCC
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM20",
                "tag" -> "",
                "barcode" -> "TCTCGCGC",
                "endbarcode" -> "GGCGACGG", //CCGTCGCC
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM27",
                "tag" -> "",
                "barcode" -> "CATCGAGG",
                "endbarcode" -> "CCTCGGAC", //GTCCGAGG
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM28",
                "tag" -> "",
                "barcode" -> "CTCGACTG",
                "endbarcode" -> "CCTCGGAC", //GTCCGAGG
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM29",
                "tag" -> "",
                "barcode" -> "CGGCTATG",
                "endbarcode" -> "CCTCGGAC", //GTCCGAGG
                "outputtype" -> "FASTQ"
              )
            ),
            "capture" -> "IlluminaTST170Dna1"
          ),
          "libRNA" -> Json.obj(
            "samples" -> Json.arr(
              Json.obj(
                "accessionid" -> "FT-SOM9",
                "tag" -> "",
                "barcode" -> "TTAATCAG",
                "endbarcode" -> "AGGCGAAG", //CTTCGCCT
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM10",
                "tag" -> "",
                "barcode" -> "CGCTCATT",
                "endbarcode" -> "TAATCTTA", //TAAGATTA
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM11",
                "tag" -> "",
                "barcode" -> "TCCGCGAA",
                "endbarcode" -> "TACTTACT", //AGTAAGTA
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM15",
                "tag" -> "",
                "barcode" -> "ATTACTCG",
                "endbarcode" -> "AGGAAGTC", //GACTTCCT
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM20",
                "tag" -> "",
                "barcode" -> "ACTGCTTA",
                "endbarcode" -> "GCGCCTCT", //AGAGGCGC
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM27",
                "tag" -> "",
                "barcode" -> "ATGCGGCT",
                "endbarcode" -> "CGCGGCTA", //TAGCCGCG
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM28",
                "tag" -> "",
                "barcode" -> "GCCTCTCT",
                "endbarcode" -> "CCTACGAA", //TTCGTAGG
                "outputtype" -> "FASTQ"
              ),
              Json.obj(
                "accessionid" -> "FT-SOM29",
                "tag" -> "",
                "barcode" -> "GCCGTAGG",
                "endbarcode" -> "GCGGAGCG", //CGCTCCGC
                "outputtype" -> "FASTQ"
              )
            ),
            "capture" -> "IlluminaTST170Rna1"
          )
        )
      )
    ))
  }
}