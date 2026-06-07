package org.example

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryLogRecord(
    val timestamp: LocalDateTime,
    val command: String

) : Comparable <HistoryLogRecord> //obiectele de tip HistoryLogRecord pot fi comparate intre ele
{
    override fun compareTo(other: HistoryLogRecord): Int
    {
        return this.timestamp.compareTo(other.timestamp)
    }

    override fun toString(): String{
        return "[$timestamp] -> $command"
    }

}

fun <T: Comparable <T>> max(first: T, second :T): T{
    val k=first.compareTo(second)
    return if (k>=0)  first else second
}

fun searchAndReplace(
    toFind: HistoryLogRecord,
    replaceWith: HistoryLogRecord,
    map: MutableMap< out Any, HistoryLogRecord> //map in care caut si inlocuiesc
) {
    var key: Any? = null

    //parcurgere toate perechile cheie->valoare din map
    for (entry in map.entries) {
        if (entry.value == toFind) {
            key = entry.key
            break
        }
    }
    //map.entries-> toate perechile din map

    if (key != null) {
        //transformare map intr un map modificabil(din cauza lui out)
        val newMap = map as MutableMap<Any, HistoryLogRecord>
        newMap[key] = replaceWith //inlocuire valoare veche cu cea noua
        println("s a inlocuit")

    } else {
        println("nu s a gasit")
    }

}
fun parseHistoryLog(path: String): MutableMap <LocalDateTime, HistoryLogRecord>
{
    val formatter= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val content= File(path).readText() //citeste tot fisierul intr un string
    val blocks=content.trim().split("\n\n")//imparte fisierul in intrari(blocuri)
    val last50=blocks.takeLast(50) //ultimele 50 de intrari
    val map=mutableMapOf <LocalDateTime, HistoryLogRecord>() //salvez rezultatul

    for(block in last50)
    {
        val lines=block.lines()
        //cautare date (linia cu data, linia cu comanda)
        val dateLine=lines.find{it.startsWith("Start-Date:")}
        val commandLine=lines.find{it.startsWith("Commandline:")}

        if(dateLine != null && commandLine !=null)
        {
            //extragere valori
            val dateStr=dateLine.removePrefix("Start-Date:").trim()
            val command= commandLine.removePrefix("Commandline:").trim()

            val timestamp=LocalDateTime.parse(dateStr, formatter) //convertire data
            val record= HistoryLogRecord(timestamp, command) //creeare obiect

            map[timestamp]=record// pun obiectul in map (cheia =timestamp, valoarea=obiectul)

        }
    }
    return map // rezultatul final
}

fun main() {
    val logMap = parseHistoryLog("history.log")
    println("inregistrari")
    logMap.values.forEach { println(it) } //afisez toate valorile(obiectele) din map

    val list = logMap.values.toList()

    if (list.size >= 2) {
        //maximul dintre primele 2 obiecte
        val maxRecord = max(list[0], list[1])
        println("Cel mai recent: $maxRecord")
    }
    if (list.size >= 2) {
        val first = list[0]
        val second = list[1]

        println("Inainte: ${logMap[first.timestamp]}")
        searchAndReplace(first, second, logMap)
        println("Dupa: ${logMap[first.timestamp]}")
    }

}