package es.upm.fi.dia.oeg.morph.base.querytranslator

import scala.collection.JavaConversions._
import Zql.ZConstant
import Zql.ZSelectItem
import org.apache.jena.graph.Triple
import org.apache.jena.vocabulary.RDF
import es.upm.fi.dia.oeg.morph.base.sql.MorphSQLSelectItem
import es.upm.fi.dia.oeg.morph.base.Constants
import es.upm.fi.dia.oeg.morph.base.model.MorphBasePropertyMapping
import es.upm.fi.dia.oeg.morph.base.model.MorphBaseMappingDocument
import es.upm.fi.dia.oeg.morph.base.model.MorphBaseClassMapping
import es.upm.fi.dia.oeg.morph.base.engine.MorphBaseUnfolder
import org.slf4j.LoggerFactory
import org.apache.jena.graph.Node

abstract class MorphBaseBetaGenerator(md:MorphBaseMappingDocument, unfolder:MorphBaseUnfolder) {
val logger = LoggerFactory.getLogger(this.getClass());
		
	val dbType = if(md.dbMetaData.isDefined) { md.dbMetaData.get.dbType; } 
	else { Constants.DATABASE_DEFAULT; }
	
	val alphaGenerator:MorphBaseAlphaGenerator=null;
	var owner:MorphBaseQueryTranslator=null;
	var mapping:Map[Node, List[ZSelectItem]]= Map.empty;
//	val databaseType = {
//		if(this.owner == null) {null}
//		else {this.owner.getDatabaseType();}
//	}
	//val dbType = md.configurationProperties.databaseType;
	
	def  calculateBeta(tp:Triple , pos:Constants.MorphPOS.Value, cm:MorphBaseClassMapping , predicateURI:String 
			, alphaResult:MorphAlphaResult ) : List[ZSelectItem] = {
		val result:List[ZSelectItem] = {
			if(pos == Constants.MorphPOS.sub) {
				this.calculateBetaSubject(tp, cm, alphaResult).toList;
			} else if(pos == Constants.MorphPOS.pre) {
				List(this.calculateBetaPredicate(predicateURI));
			} else if(pos == Constants.MorphPOS.obj) {
				val predicateIsRDFSType = RDF.`type`.getURI().equals(predicateURI);
				if(predicateIsRDFSType) {
					val className = new ZConstant(cm.getConceptName(), ZConstant.STRING);
					val selectItem = MorphSQLSelectItem.apply(className);
					//selectItem.setExpression(className);
					List(selectItem);
				} else {
					this.calculateBetaObject(tp, cm, predicateURI, alphaResult).toList;	
				}
			} else {
				throw new Exception("invalid Pos value in beta!");
			}
		}
		logger.debug("beta = " + result);
		return result;
	}

	def calculateBetaObject(triple:Triple, cm:MorphBaseClassMapping , predicateURI:String , alphaResult:MorphAlphaResult )
	: List[ZSelectItem] = {
		val betaObjects:List[ZSelectItem]  = {
			val pms = cm.getPropertyMappings(predicateURI);
			if(pms == null || pms.isEmpty()) {
				val errorMessage = "Undefined mappings for : " + predicateURI + " for class " + cm.getConceptName();
				logger.debug(errorMessage);
				Nil;
			} else if (pms.size() > 1) {
				val errorMessage = "Multiple property mappings defined, result may be wrong!";
				logger.debug(errorMessage);
				//throw new Exception(errorMessage);
				val pm = pms.iterator.next();
				this.calculateBetaObject(triple, cm, predicateURI, alphaResult, pm).toList;
			} else {//if(pms.size() == 1)
				val pm = pms.iterator.next();
				this.calculateBetaObject(triple, cm, predicateURI, alphaResult, pm).toList;
			}		  
		}
		if (triple.getObject.isVariable())
		    if (mapping.isEmpty)
		      mapping+=(triple.getObject->betaObjects)
		    else if (!mapping.contains(triple.getObject)) {
    		  mapping+=(triple.getObject->betaObjects)
		}
		if (triple.getSubject.isVariable()) {
		  if (mapping.isEmpty)
		      mapping+=(triple.getSubject->this.calculateBetaSubject(triple, cm, alphaResult))
		    else if (!mapping.contains(triple.getSubject)) {
    		  mapping+=(triple.getSubject->this.calculateBetaSubject(triple, cm, alphaResult))
		}
		}
		return betaObjects;		
	}

	def  calculateBetaObject(triple:Triple, cm:MorphBaseClassMapping , predicateURI:String 
	    , alphaResult:MorphAlphaResult, pm:MorphBasePropertyMapping ) : List[ZSelectItem] ;

	def  calculateBetaPredicate(predicateURI:String ) : ZSelectItem = {
		val predicateURIConstant = new ZConstant(predicateURI, ZConstant.STRING);
		val selectItem = MorphSQLSelectItem.apply(predicateURIConstant);
		selectItem;
	}
	
	def calculateBetaSubject(tp:Triple , cm:MorphBaseClassMapping , alphaResult:MorphAlphaResult ) : List[ZSelectItem];

}