@Grapes(
	@Grab(group='org.codehaus.gpars', module='gpars', version='1.2.1')
)


import static groovyx.gpars.GParsPool.withPool

System.properties.putAll( ["proxySet":"true","socksProxyHost":"localhost", "socksProxyPort":"9050"] )
def spider(address){
    try {

    String data = new URL(address).getText()
    def uniqueOnions = data.findAll(/(?:([a-z]+):\/\/){0,1}([a-z2-7]{16})\.onion(?::(\d+)){0,1}/).unique()
    if(uniqueOnions){
	uniqueOnions.eachParallel{onion ->
	    testConnection(onion)
            spider(onion)
        }
    }
} catch (java.io.IOException ex){
    println "$address Failed - Can't Spider"
}
}


def testConnection(address){
    URL url = new URL(address)
    HttpURLConnection connection = (HttpURLConnection)url.openConnection()
    connection.setRequestMethod("GET")
    try {
        connection.connect()
        println "$address - ${connection.getResponseCode()}"
    } catch(java.net.SocketException ex){
        println "$address - No Connection"
    }
}

withPool{
spider('http://thehiddenwiki.org')
}
