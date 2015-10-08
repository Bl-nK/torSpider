@Grapes([
    @Grab(group='org.xerial',module='sqlite-jdbc',version='3.7.2'),
    @Grab(group='org.codehaus.gpars', module='gpars', version='1.2.1'),
    @GrabConfig(systemClassLoader=true)
])

import static groovyx.gpars.GParsPool.withPool
import classes.*
new DataMan().createDB()

System.properties.putAll( ["proxySet":"true","socksProxyHost":"localhost", "socksProxyPort":"9050"] )
void spider(String address){
    DataMan dataMan = new DataMan()
    try {
        String data = new URL(address).getText([connectTimeout: 10000, readTimeout: 30000])
        def uniqueOnions = data.findAll(/(?:([a-z]+):\/\/){0,1}([a-z2-7]{16})\.onion(?::(\d+)){0,1}/).unique()
        println "Found ${uniqueOnions.size()} unique onions at $address"
        if(uniqueOnions){
            uniqueOnions = uniqueOnions.collect{
                if(!it.startsWith('http://') && !it.startsWith('https://')){
                    it = "http://$it"
                }
                else {
                    it = it
                }
            }
            dataMan.addOnions(uniqueOnions,address)
        }
    }
    catch (java.io.IOException ex){
        println "$address Failed - Can't Spider - $ex"
    }
}

Boolean testConnection(String address){
    CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    URL url = new URL(address)
    HttpURLConnection connection = (HttpURLConnection)url.openConnection()
    connection.setRequestMethod("GET")
    File testedUrls = new File(new File(getClass().protectionDomain.codeSource.location.path).parent + "/testedUrls.txt")
    File successfulUrls = new File(new File(getClass().protectionDomain.codeSource.location.path).parent + "/successfulUrls.txt")
    if (!testedUrls.text.find(address)){
        testedUrls << "$address${System.getProperty("line.separator")}"
        try {
            connection.connect()
            println "$address - ${connection.getResponseCode()}"
            if (connection.getResponseCode() == 200){
		successfulUrls << "$address${System.getProperty("line.separator")}"
                return true
                } else {
                    return false
                }
            }
            catch(java.net.SocketException ex){
                println "$address - No Connection - $ex"
                return false
            }
            catch(java.net.ProtocolException ex){
                println "$address - No Connection - $ex"
                return false
            }
    }
    else {
        return false
    }

}

withPool{
    spider(args[0])
}
