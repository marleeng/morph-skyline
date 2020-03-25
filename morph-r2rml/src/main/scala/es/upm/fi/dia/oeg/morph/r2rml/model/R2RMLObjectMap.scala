package es.upm.fi.dia.oeg.morph.r2rml.model

import scala.collection.JavaConversions._
import org.apache.jena.rdf.model.Resource
import es.upm.fi.dia.oeg.morph.base.Constants
import org.apache.jena.rdf.model.RDFNode
import org.slf4j.LoggerFactory

class R2RMLObjectMap(termMapType:Constants.MorphTermMapType.Value
    , termType:Option[String], datatype:Option[String], languageTag:Option[String]) 
    extends R2RMLTermMap(termMapType, termType, datatype, languageTag) {
  
	override   val logger = LoggerFactory.getLogger(this.getClass());


}

object R2RMLObjectMap {
	def apply(rdfNode:RDFNode) : R2RMLObjectMap = {
		val coreProperties = R2RMLTermMap.extractCoreProperties(rdfNode);
		//coreProperties = (termMapType, termType, datatype, languageTag)
		val termMapType = coreProperties._1;
		val termType = coreProperties._2;
		val datatype = coreProperties._3;
		val languageTag = coreProperties._4;
		val om = new R2RMLObjectMap(termMapType, termType, datatype, languageTag);
		om.parse(rdfNode);
		om;	  
	}
	
	def extractObjectMaps(resource:Resource) : Set[R2RMLObjectMap] = {
	  val tms = R2RMLTermMap.extractTermMaps(resource, Constants.MorphPOS.obj);
	  val result = tms.map(tm => tm.asInstanceOf[R2RMLObjectMap]);
	  result;
	}
}