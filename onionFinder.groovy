@Grapes([
    @Grab(group='org.xerial',module='sqlite-jdbc',version='3.7.2'),
    @Grab(group='org.codehaus.gpars', module='gpars', version='1.2.1'),
    @GrabConfig(systemClassLoader=true)
])

import static groovyx.gpars.GParsPool.withPool
import classes.*


DataMan dataMan = new DataMan()

System.properties.putAll( ["proxySet":"true","socksProxyHost":"localhost", "socksProxyPort":"9050"] )
void spider(String address, DataMan dataMan){
    try {
        String data = new URL(address).getText([connectTimeout: 10000, readTimeout: 30000])
        def uniqueOnions = data.findAll(/(?:([a-z]+):\/\/){0,1}([a-z2-7]{16})\.onion(?::(\d+)){0,1}/).unique()
        println "Found ${uniqueOnions.size()} unique onions at $address"
        if(uniqueOnions){
            withPool{
                uniqueOnions = uniqueOnions.collectParallel{
                    if(!it.startsWith('http://') && !it.startsWith('https://')){
                        it = "http://$it"
                    }
                    else {
                        it = it
                    }
                }
                dataMan.addOnions(uniqueOnions,address)
                dataMan.modifyRecord(address,'spider',true, true)
            }
        }
    }
    catch (java.io.IOException ex){
        println "$address Failed - Can't Spider - $ex"
    }
}

Boolean testConnection(String address, DataMan dataMan){
    CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    URL url = new URL(address)
    HttpURLConnection connection = (HttpURLConnection)url.openConnection()
    connection.setRequestMethod("GET")
        try {
            connection.connect()
            println "$address - ${connection.getResponseCode()}"
            if (connection.getResponseCode() == 200){
                dataMan.modifyRecord(address,'test', true, true)                    
            } else {
                dataMan.modifyRecord(address,'test', true, false)
            }
        }
        catch(java.net.SocketException ex){
            println "$address - No Connection - $ex"
        }
        catch(java.net.ProtocolException ex){
            println "$address - No Connection - $ex"
        }
}

if (args.size() < 1) {
    println "Please supply a URL to begin the spider process"
    System.exit(1)
}

dataMan.createDB()

withPool{
    spider(args[0], dataMan) //Spider the given URL
    while(true) {
        def onionsToTest = dataMan.getOnionsForTesting('test') //Create a list of onions to be tested
        onionsToTest.eachParallel{testConnection(it.url, dataMan)} //Test each onion
        onionsToSpider = dataMan.getOnionsForTesting('spider')//Create a list of onions eligible for spider
        onionsToSpider.eachParallel{spider(it.url, dataMan)}//Spider onions
    }
}
