package classes

import java.sql.*
import org.sqlite.SQLite
import groovy.sql.Sql

class DataMan{

	static Sql sql = Sql.newInstance("jdbc:sqlite:onion.db", "org.sqlite.JDBC")

	def createDB(){
	    sql.execute("drop table if exists onions")
	    sql.execute("create table onions (id integer primary key, url string, parent integer references onions (id), spidered boolean, tested boolean, status boolean)")

	}

	def addOnions(List onions, String parent){
	    sql.execute("PRAGMA foreign_keys = ON")
	    sql.execute("PRAGMA synchronous = OFF")
	    def parentId = sql.executeInsert("insert or ignore into onions (url,spidered,tested,status) values (?,?,?,?)",[parent,true,true,1])
		sql.withBatch(1000,"insert into onions (url,parent,spidered,tested,status) values (?,?,?,?,?)"){ ps ->
			onions.each{
				ps.addBatch(it,parentId[0][0],false,false,null)
			}
		}	    

	    //onions.each{
		//	def insertStatement = sql.execute("insert into onions (url,parent,spidered,tested,status) values (?,?,?,?,?)",[it,parentId[0][0],false,false,null])
	    //}
	}

	def getOnionsForTesting(String operation){
	    if (operation == "test"){
	        def toTest = sql.rows('select url from onions where not (tested)')
	    } else if (operation == "spider"){
	        def toTest = sql.rows('select url from onions where tested = 1 AND spidered = 0 AND status = 1')
	    }
	}

	def modifyRecord(String url, String type, Boolean value, Boolean status){
		if (type == "test"){
	        def tested = sql.executeUpdate('update onions set tested = ?, status = ? where url = ?',[value, status, url])
	    } else if (type == "spider"){
	        def spidered = sql.executeUpdate('update onions set spidered = ? where url = ?',[value, url])
	    }
	}

}
