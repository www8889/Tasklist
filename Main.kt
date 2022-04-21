package tasklist
import kotlinx.datetime.*
import com.squareup.moshi.*
import java.io.File

var taskItemListForjson: MutableList<TaskItemForjson> = mutableListOf()
var taskList: MutableList<String> = mutableListOf()
var taskItemList: MutableList<TaskItem> = mutableListOf()
class TaskItem {
    var priority: String = ""
    var date: String = ""
    var time: String = ""
    var timed: LocalDateTime = LocalDateTime(1,1,1,1,1)
    var taskstr: String = ""
}
class TaskItemForjson {
    var priority: String = ""
    var date: String = ""
    var time: String = ""
    var taskstr: String = ""
}
fun main(args: Array<String>) {
    val jsonFile = File("tasklist.json")
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val type = Types.newParameterizedType(List::class.java, TaskItemForjson::class.java)
    val taskItemListAdapter = moshi.adapter<List<TaskItemForjson?>>(type)
    if (jsonFile.exists()) {
        val jsonStr: String = jsonFile.readText()
        //println(jsonStr)
        var taskItemListForjson1 = taskItemListAdapter.fromJson(jsonStr)
        for (item in taskItemListForjson1!!) {
            if (item != null) {
                //println(item.taskstr)
                taskItemListForjson.add(item)
            }
        }
        //taskItemListForjson.forEach { println(it.date) }
    }
    do {
        println("Input an action (add, print, edit, delete, end):")
        when (readln().lowercase()) {
            "add" -> add_task()
            "print" -> print_task()
            "edit" -> edit_task()
            "delete" -> delete_task()
            "end" -> {
                println("Tasklist exiting!")
                break
            }
            else -> println("The input action is invalid")
        }
    }while(true)
    val str = taskItemListAdapter.toJson(taskItemListForjson.toList())
    //println(str)
    jsonFile.writeText(str)
}
fun add_task() {
    println("Input the task priority (C, H, N, L):")
    var priority = readln().uppercase()
    while (!"[CHNL]".toRegex().matches(priority)) {
        println("Input the task priority (C, H, N, L):")
        priority = readln().uppercase()
    }
    println("Input the date (yyyy-mm-dd):")
    var datestr = readln()
    var datelist:List<String>
    var date:LocalDate
    while (true) {
        if ("\\d{4}-\\d{1,2}-\\d{1,2}".toRegex().matches(datestr)) {
            datelist = datestr.split("-").toList()
            try {
                date = LocalDate(datelist[0].toInt(), datelist[1].toInt(), datelist[2].toInt())
                break
            } catch (e: IllegalArgumentException) {
                println("The input date is invalid")
            }
        } else {
            println("The input date is invalid")
        }
        println("Input the date (yyyy-mm-dd):")
        datestr = readln()
    }
    //println(date)
    //println(date.toString())
    println("Input the time (hh:mm):")
    var timestr = readln()
    var timelist:List<String>
    var time:LocalDateTime
    while (true) {
        if ("\\d{1,2}:\\d{1,2}".toRegex().matches(timestr)) {
            timelist = timestr.split(":").toList()
            try {
                time = LocalDateTime(date.year, date.month, date.dayOfMonth,
                    timelist[0].toInt(), timelist[1].toInt())
                break
            } catch (e: IllegalArgumentException) {
                println("The input time is invalid")
            }
        } else {
            println("The input time is invalid")
        }
        println("Input the time (hh:mm):")
        timestr = readln()
    }
    println("Input a new task (enter a blank line to end):")
    var tempstr = ""
    var taskstr = ""
    var readline = readln()
    var pre_line_len = 0
    if ("\\s*".toRegex().matches(readline))
        println("The task is blank")
    else {
        var taskItem = TaskItem()
        do {
            readline = readline.trim() //去除当前字符串两边的空白字符
            if ("\\s*".toRegex().matches(readline))
                break
            if (pre_line_len>0) {
                val temp = 44 - pre_line_len % 44
                repeat(temp) {
                    taskstr += " "
                }
            }
            taskstr += readline
            pre_line_len = readline.length
            readline = readln()
        } while (true)

        taskItem.date = time.date.toString()
        taskItem.time = ""+time.hour+":"+time.minute
        taskItem.priority = priority
        taskItem.taskstr = taskstr
        taskItem.timed = time
        taskItemList.add(taskItem)

        var taskItemjson = TaskItemForjson()
        taskItemjson.date = time.date.toString()
        taskItemjson.time = ""+time.hour+":"+time.minute
        taskItemjson.priority = priority
        taskItemjson.taskstr = taskstr
        taskItemListForjson.add(taskItemjson)



    }
}
fun print_task() {
    if (taskItemListForjson.isEmpty())
        println("No tasks have been input")
    else {
        println("+----+------------+-------+---+---+--------------------------------------------+")
        println("| N  |    Date    | Time  | P | D |                   Task                     |")
        println("+----+------------+-------+---+---+--------------------------------------------+")
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        for (index in taskItemListForjson.indices) {
            print("| ${index +1}")
            if(index < 9) {
                print("  ")
            } else {
                print(" ")
            }
            val dlist = taskItemListForjson[index].date.split("-")
            val taskDate = LocalDate(dlist[0].toInt(), dlist[1].toInt(), dlist[2].toInt())
            //val taskDate = taskItemList[index].timed.date//LocalDate(2022, 1, 9)
            val numberOfDays = currentDate.daysUntil(taskDate)
            val priority = when (taskItemListForjson[index].priority) {
                "C" -> "\u001B[101m \u001B[0m"
                "H" -> "\u001B[103m \u001B[0m"
                "N" -> "\u001B[102m \u001B[0m"
                "L" -> "\u001B[104m \u001B[0m"
                else -> "\u001B[101m \u001B[0m"
            }
            val tag = if(numberOfDays==0)
                "\u001B[103m \u001B[0m"
            else if(numberOfDays<0)
                "\u001B[101m \u001B[0m"
            else
                "\u001B[102m \u001B[0m"
            var temp: Int = 0
            var timestr = ""
            when (taskItemListForjson[index].time.indexOf(":",0,true)) {
                1 -> timestr =
                    if (taskItemListForjson[index].time.length==3)
                        "0${taskItemListForjson[index].time[0]}:0${taskItemListForjson[index].time[2]}"
                    else "0${taskItemListForjson[index].time[0]}:${taskItemListForjson[index].time[2]}${taskItemListForjson[index].time[3]}"

                else -> timestr =
                    if (taskItemListForjson[index].time.length==4){
                        "${taskItemListForjson[index].time[0]}${taskItemListForjson[index].time[1]}:0${taskItemListForjson[index].time[3]}"
                    }else taskItemListForjson[index].time
            }
            print("| ${taskItemListForjson[index].date} | $timestr | " +
                    priority+ " | " + tag + " |")
            val taskstrline = taskItemListForjson[index].taskstr.length / 44
            if(taskItemListForjson[index].taskstr.length >= 44) {
                print(""+taskItemListForjson[index].taskstr.subSequence(0, 44)+"|\n")
                if(taskstrline>1) {
                    for (line in 2..taskstrline) {
                        print("|    |            |       |   |   |")
                        print("" + taskItemListForjson[index].taskstr.subSequence((line - 1) * 44, line * 44) + "|\n")
                    }
                }
                temp = (taskstrline + 1) * 44 - taskItemListForjson[index].taskstr.length
                if(temp > 0 && temp < 44) {
                    print("|    |            |       |   |   |")
                    print("${taskItemListForjson[index].taskstr.subSequence(taskstrline * 44, taskItemListForjson[index].taskstr.lastIndex+1)}")
                    repeat(temp) {
                        print(" ")
                    }
                    print("|\n")
                }
            } else {
                temp = 44 - taskItemListForjson[index].taskstr.length
                print("${taskItemListForjson[index].taskstr.subSequence(0, taskItemListForjson[index].taskstr.lastIndex+1)}")
                repeat(temp) {
                    print(" ")
                }
                print("|\n")
            }
            print("+----+------------+-------+---+---+--------------------------------------------+\n")
        }
    }
}
fun delete_task() {
    if(taskItemListForjson.isEmpty())
        println("No tasks have been input")
    else {
        print_task()
        while (true) {
            println("Input the task number (1-${taskItemListForjson.size}):")
            val del_task_number = readln().toIntOrNull()
            if (del_task_number != null) {
                if(del_task_number <= taskItemListForjson.size && del_task_number > 0) {
                    taskItemListForjson.removeAt(del_task_number-1)
                    println("The task is deleted")
                    break
                } else {
                    println("Invalid task number")
                }
            }else println("Invalid task number")
        }
    }
}
fun edit_task() {
    if(taskItemListForjson.isEmpty())
        println("No tasks have been input")
    else {
        print_task()
        while (true) {
            println("Input the task number (1-${taskItemListForjson.size}):")
            val edit_task_number = readln().toIntOrNull()
            if (edit_task_number!=null) {
                if (edit_task_number <= taskItemListForjson.size && edit_task_number > 0) {
                    while (true) {
                        println("Input a field to edit (priority, date, time, task):")
                        val readline = readln().lowercase()
                        when (readline) {
                            "priority" -> {
                                println("Input the task priority (C, H, N, L):")
                                var priority = readln().uppercase()
                                while (!"[CHNL]".toRegex().matches(priority)) {
                                    println("Input the task priority (C, H, N, L):")
                                    priority = readln().uppercase()
                                }
                                taskItemListForjson[edit_task_number - 1].priority = priority
                                break
                            }
                            "date" -> {
                                println("Input the date (yyyy-mm-dd):")
                                var datestr = readln()
                                var datelist: List<String>
                                var date: LocalDate
                                while (true) {
                                    if ("\\d{4}-\\d{1,2}-\\d{1,2}".toRegex().matches(datestr)) {
                                        datelist = datestr.split("-").toList()
                                        try {
                                            date = LocalDate(
                                                datelist[0].toInt(), datelist[1].toInt(), datelist[2].toInt()
                                            )
                                            break
                                        } catch (e: IllegalArgumentException) {
                                            println("The input date is invalid")
                                        }
                                    } else {
                                        println("The input date is invalid")
                                    }
                                    println("Input the date (yyyy-mm-dd):")
                                    datestr = readln()
                                }
                                taskItemListForjson[edit_task_number - 1].date = date.toString()
                                break
                            }
                            "time" -> {
                                println("Input the time (hh:mm):")
                                var timestr = readln()
                                var timelist: List<String>
                                var time: LocalDateTime
                                val dlist = taskItemListForjson[edit_task_number-1].date.split("-")
                                while (true) {
                                    if ("\\d{1,2}:\\d{1,2}".toRegex().matches(timestr)) {
                                        timelist = timestr.split(":").toList()
                                        try {
                                            time = LocalDateTime(
                                                dlist[0].toInt(), dlist[1].toInt(), dlist[2].toInt(),
                                                timelist[0].toInt(), timelist[1].toInt()
                                            )
                                            break
                                        } catch (e: IllegalArgumentException) {
                                            println("The input time is invalid")
                                        }
                                    } else {
                                        println("The input time is invalid")
                                    }
                                    println("Input the time (hh:mm):")
                                    timestr = readln()
                                }
                                taskItemListForjson[edit_task_number - 1].time = timestr
                                break
                            }
                            "task" -> {
                                println("Input a new task (enter a blank line to end):")
                                var taskstr = ""
                                var readline = readln()
                                var pre_line_len = 0
                                if ("\\s*".toRegex().matches(readline))
                                    println("The task is blank")
                                else {
                                    do {
                                        readline = readline.trim() //去除当前字符串两边的空白字符
                                        if ("\\s*".toRegex().matches(readline))
                                            break
                                        if (pre_line_len>0) {
                                            val temp = 44 - pre_line_len % 44
                                            repeat(temp) {
                                                taskstr += " "
                                            }
                                        }
                                        taskstr += readline
                                        pre_line_len = readline.length
                                        readline = readln()
                                    } while (true)
                                }
                                taskItemListForjson[edit_task_number - 1].taskstr = taskstr
                                break
                            }
                            else -> println("Invalid field")
                        }
                    }
                    println("The task is changed")
                    break
                } else {
                    println("Invalid task number")
                }
            } else println("Invalid task number")
        }
    }
}

