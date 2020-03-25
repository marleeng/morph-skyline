package es.upm.fi.dia.oeg.morph.r2rml.model

import es.upm.fi.dia.oeg.morph.base.model.MorphBaseLogicalTable
import es.upm.fi.dia.oeg.morph.base.sql.MorphDatabaseMetaData
import es.upm.fi.dia.oeg.morph.base.sql.MorphTableMetaData
import org.apache.jena.rdf.model.Resource
import es.upm.fi.dia.oeg.morph.base.Constants
import es.upm.fi.dia.oeg.morph.r2rml.MorphR2RMLElementVisitor
import es.upm.fi.dia.oeg.morph.r2rml.MorphR2RMLElement
import org.slf4j.LoggerFactory

//import es.upm.fi.dia.oeg.morph.r2rml.model.R2RMLSQLQuery
//import es.upm.fi.dia.oeg.morph.r2rml.model.R2RMLTable

abstract class R2RMLLogicalTable(val logicalTableType:Constants.LogicalTableType.Value) 
extends MorphBaseLogicalTable with MorphR2RMLElement{
  val logger = LoggerFactory.getLogger(this.getClass());
	var alias:String = null;

	def buildMetaData(dbMetaData:Option[MorphDatabaseMetaData] ) = {
		val tableName = this match {
		  case r2rmlTable:R2RMLTable => {
		    val dbType = if(dbMetaData.isDefined) { dbMetaData.get.dbType; }
		    else { Constants.DATABASE_DEFAULT }
		      
		    val enclosedChar = Constants.getEnclosedCharacter(dbType);
		    val tableNameAux = r2rmlTable.getValue().replaceAll("\"", enclosedChar);
		    tableNameAux
		  }
		  case r2rmlSQLQuery:R2RMLSQLQuery => {
			val queryStringAux = r2rmlSQLQuery.getValue().trim();
			val queryString = {
				if(queryStringAux.endsWith(";")) {
					queryStringAux.substring(0, queryStringAux.length()-1);
				} else {queryStringAux}
			}
			"(" + queryString + ")";		    
		  }
		}
		
		if(dbMetaData.isDefined) {
			val optionTableMetaData = dbMetaData.get.getTableMetaData(tableName);
			val tableMetaData = optionTableMetaData.getOrElse(
			    MorphTableMetaData.buildTableMetaData(tableName, dbMetaData.get));
	
			this.tableMetaData = Some(tableMetaData);
		}

	}

	def getValue() : String;

	override def toString() : String = {
		val result = this match {
		  case _:R2RMLTable => { "R2RMLTable"; }
		  case _:R2RMLSQLQuery => { "R2RMLSQLQuery"; } 
		  case _ => { "" }		  
		}
		  
		result + ":" + this.getValue();
	}


	def accept(visitor:MorphR2RMLElementVisitor ) : Object  = {
		val result = visitor.visit(this);
		result;
	}
}

object R2RMLLogicalTable {
  val logger = LoggerFactory.getLogger(this.getClass());

	def parse(resource:Resource ) : R2RMLLogicalTable  = {
		 
		val tableNameStatement = resource.getProperty(Constants.R2RML_TABLENAME_PROPERTY);
		val logicalTable : R2RMLLogicalTable = if(tableNameStatement != null) {
			val tableName = tableNameStatement.getObject().toString();
			new R2RMLTable(tableName);			
		} else {
			val sqlQueryStatement = resource.getProperty(
					Constants.R2RML_SQLQUERY_PROPERTY);
			if(sqlQueryStatement == null) {
				logger.error("Invalid logical table defined : " + resource);
			}
			val sqlQueryStringAux = sqlQueryStatement.getObject().toString().trim();
			val sqlQueryString = if(sqlQueryStringAux.endsWith(";")) {
				sqlQueryStringAux.substring(0, sqlQueryStringAux.length()-1);
			} else {
			  sqlQueryStringAux
			}
			
			new R2RMLSQLQuery(sqlQueryString);
		}

		logicalTable;
	}  
}