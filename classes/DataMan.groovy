package classes

import java.sql.*
import org.sqlite.SQLite
import groovy.sql.Sql

class DataMan{

	static Sql sql = Sql.newInstance("jdbc:sqlite:onion.db", "org.sqlite.JDBC")


	def createDB(){
		println sql.getClass()
		sql.execute("drop table if exists onions")
		sql.execute("create table onions (id integer primary key, url string, parent integer references onions (id), spidered boolean, tested boolean)")

	}

	def addOnions(List onions, String parent){
	}

}
