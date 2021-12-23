package com.example.sudoku

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_best_list.*
import java.text.SimpleDateFormat
import java.util.*

private lateinit var prefs: SharedPreferences
private lateinit var bestListTextViews: List<TextView>
class Best_list : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_best_list)
        bestListTextViews = listOf(time1,time2,time3,time4,time5,time6,time7,time8)
        prefs = getSharedPreferences("bestlist", Context.MODE_PRIVATE)
        var times:MutableMap<String,Long> = prefs.all as MutableMap<String, Long>
        var timesTemp:MutableList<Long>
        timesTemp = mutableListOf()
        for((key,time) in times)
            timesTemp.add(time)

        timesTemp=timesTemp.toSortedSet().toMutableList()
        for(i in 0..times.size-1)
        {
            bestListTextViews[i].text = convertLongToTime(timesTemp[i])
        }
    }
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("mm:ss")
        return format.format(date)
    }
}