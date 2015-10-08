import java.sql.*
import org.sqlite.SQLite
import groovy.sql.Sql

class DataMan{

	static Sql sql = Sql.newInstance("jdbc:sqlite:onion.db", "org.sqlite.JDBC")


	def createDB(){
		println sql.getClass()
		sql.execute("drop table if exists onions")
		sql.execute("create table onions (child string, parent string, spidered boolean, tested boolean)")

	}

	def addOnions(List onions, String parent){
		def dataset = sql.dataSet('onions')
		onions.each{
			dataset.add(parent:parent,child:it,spidered:false,tested:false)
		}
	}

}