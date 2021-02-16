import groovy.xml.*;

class SysFile {
    File fileIn;
    File fileOut;
    Node xml;

    SysFile(File fileIn, File fileOut) {
        this.fileIn = fileIn
        this.fileOut = fileOut
    }

    Node parse() {
        xml = new XmlParser().parse(fileIn)
        xml
    }

    def writeOut() {
        fileOut.write(XmlUtil.serialize(xml))
    }
}

def settlements = new SysFile(
        new File("H:\\SteamLibrary\\steamapps\\common\\Mount & Blade II Bannerlord\\Modules\\AliceTriesTown\\ModuleData\\settlements.xml"),
        new File("settlements.xml")
)

def scene = new SysFile(
        new File("H:\\SteamLibrary\\steamapps\\common\\Mount & Blade II Bannerlord\\Modules\\AliceTriesTown\\SceneObj\\Main_map\\scene.xscene"),
        new File("scene.xscene")
)

/********
 * MAIN *
 ********/
println("Loading")
settlements.parse()
scene.parse()


def modifyId(String id) {
    id + "69"
}
def shorten(String id){
    if(id.endsWith("69"))
        return id.substring(0, id.size()-2)
    return id;
}
//because groovy is shit and [does this]
String id(Node n){
    n.@id
}
String bound(Node n){
    n.@bound
}
String trade_bound(Node n){
    n.@trade_bound
}
String posY(Node n){
    n.@posY
}
String posX(Node n){
    n.@posX
}
//String average(String v1, String v2){
//    v1.toBigDecimal().plus(v2.toBigDecimal()).div(2.0).toBigDecimal()
//}

println("Processing")

def towns = [:]

def villages = [:]
//def idCloneList = []
////not used but kept for report
//def idCompCloneList = []

settlements.xml.Settlement.eachWithIndex {
    x, i ->
        if (!x.Components.Village) {

            x.parent().remove(x)
            towns.put("Settlement.${id(x)}".toString(), x)

        }
}
settlements.xml.Settlement.eachWithIndex {
    x, i ->
        if (!x.Components.Village) {
            println "Should not happen"

        } else {
            def village = x.Components.Village;

            //save ids for scene.xscene
//            idCloneList << id(x)
//            idCompCloneList << id(village)
            villages.put(shorten(id(x)), x);

//            x.@id = modifyId(id(x))
//            x.@name = "Clone Village ${i}"

//            village.@id = modifyId(id(village))

            //average location for this
            //can use == is groovy
//            if(trade_bound(village) == bound(village)){
//                def town = towns.get(bound(village))
//                if(town){
////                    x.@posX = average(posX(town), posX(x))
////                    x.@posY = average(posY(town), posY(x))
//                }else{
//                    println( "Well thats odd ${x.@name} has no town ${bound(village)}" )
//                }
//            } else {
//                println( "Well thats odd ${x.@name} has weird boundings" )
//            }


        }
}

println "Villages Processed"

scene.xml.entities.game_entity.findAll({
    Node x->
        x.@name && villages[x.@name]
}).eachWithIndex {
    Node x,i ->
        def village = villages[x.@name]
        def clone = x.clone()
        clone.@name = modifyId(clone.@name)
        assert x.@name != clone.@name

        def t = clone.transform;
        String posString = t.@position

        t.@position = "${posX(village)}, ${posY(village)}${posString.substring(posString.lastIndexOf(','), posString.lastIndexOf(']'))}";

        def cloned = x.parent().append(clone);
        println "${i} : ${x.@name}@${x.transform.@position} adding ${clone.@name}@${clone.transform.@position} - ${cloned} "





}


println("Saving")
//settlements.writeOut()
scene.writeOut()
println("Done[]")
