package classes

import java.sql.*
import org.sqlite.SQLite
import groovy.sql.Sql

class DataMan{

	static Sql sql = Sql.newInstance("jdbc:sqlite:onion.db", "org.sqlite.JDBC")

	def createDB(){
	    sql.execute("drop table if exists onions")
	    sql.execute("create table onions (id integer primary key, url string, parent integer references onions (id), spidered boolean, tested boolean, status string)")

	}

	def addOnions(List onions, String parent){
	    sql.execute("PRAGMA foreign_keys = ON")
	    def parentId = sql.executeInsert("insert or ignore into onions (url,spidered,tested,status) values (?,?,?,?)",[parent,true,true,'success'])
	    onions.each{
			def insertStatement = sql.execute("insert into onions (url,parent,spidered,tested,status) values (?,?,?,?,?)",[it,parentId[0][0],false,false,null])
	    }
	}

	def getOnionsForTesting(String operation){
	    if (operation == "test"){
	        def toTest = sql.rows('select url from onions where not (tested)')
	    } else if (operation == "spider"){
	        def toTest = sql.rows('select url from onions where tested = 1 AND spidered = 0')
	    }
	}

	def modifyRecord(String url, String type, Boolean value){
		if (type == "test"){
	        def tested = sql.executeUpdate('update onions set tested = ? where url = ?',[value, url])
	    } else if (type == "spider"){
	        def spidered = sql.executeUpdate('update onions set spidered = ? where url = ?',[value, url])
	    }
	}

}
