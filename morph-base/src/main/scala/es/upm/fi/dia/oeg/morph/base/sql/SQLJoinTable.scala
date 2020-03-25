package es.upm.fi.dia.oeg.morph.base.sql

import scala.collection.JavaConversions._
import Zql.ZFromItem
import Zql.ZExpression
import Zql.ZExp
import es.upm.fi.dia.oeg.morph.base.Constants
import org.slf4j.LoggerFactory

class SQLJoinTable(val joinSource:SQLLogicalTable ,val joinType:String 
    , var onExpression:ZExpression ) extends ZFromItem {
	val logger = LoggerFactory.getLogger(this.getClass());
	
	def this(joinSource:SQLLogicalTable ) = {
		this(joinSource, null, null);
	}
	
	def addOnExpression(onExp2:ZExpression ) = {
		val expressionsList = Set(this.onExpression, onExp2);
		
		this.onExpression = MorphSQLUtility.combineExpresions(
		    expressionsList, Constants.SQL_LOGICAL_OPERATOR_AND);
	}
	
	override def toString() : String  = {
		var result = "";
		if(this.joinType != null) {
			result += this.joinType + " JOIN ";	
		}
		result += this.joinSource;
		if(this.onExpression != null) {
			result += " ON " + this.onExpression;	
		}
		
		result;
	}


}