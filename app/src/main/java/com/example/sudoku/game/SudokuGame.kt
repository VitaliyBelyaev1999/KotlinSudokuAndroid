package com.example.sudoku.game

import androidx.lifecycle.MutableLiveData
import android.widget.Chronometer
import android.content.SharedPreferences
import android.os.SystemClock
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random


class SudokuGame() {

    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<List<Cell>>()
    val isTakingNotesLiveData = MutableLiveData<Boolean>()
    val highlightedKeysLiveData = MutableLiveData<Set<Int>>()

    private var selectedRow = -1
    private var selectedCol = -1
    private var isTakingNotes = false

    private var sqrtSize = 3
    private var size = 9
    private val board: Board

    init {
        val cells = MutableList(9*9) {i -> Cell(i/9, i % 9, 0)}

        /*cells[4].value=3
        cells[4].isStartingCell=true
        cells[7].value=8
        cells[7].isStartingCell=true
        cells[9].value=6
        cells[9].isStartingCell=true
        cells[10].value=3
        cells[10].isStartingCell=true
        cells[13].value=7
        cells[13].isStartingCell=true
        cells[17].value=9
        cells[17].isStartingCell=true
        cells[19].value=1
        cells[19].isStartingCell=true
        cells[22].value=6
        cells[22].isStartingCell=true
        cells[25].value=5
        cells[25].isStartingCell=true
        cells[27].value=8
        cells[27].isStartingCell=true
        cells[29].value=3
        cells[29].isStartingCell=true
        cells[38].value=2
        cells[38].isStartingCell=true
        cells[39].value=1
        cells[39].isStartingCell=true
        cells[44].value=8
        cells[44].isStartingCell=true
        cells[46].value=9
        cells[46].isStartingCell=true
        cells[48].value=4
        cells[48].isStartingCell=true
        cells[52].value=2
        cells[52].isStartingCell=true
        cells[55].value=2
        cells[55].isStartingCell=true
        cells[58].value=1
        cells[58].isStartingCell=true
        cells[60].value=7
        cells[60].isStartingCell=true
        cells[73].value=6
        cells[73].isStartingCell=true
        cells[75].value=5
        cells[75].isStartingCell=true
        cells[79].value=9
        cells[79].isStartingCell=true*/


        generating(cells)


        cells[0].notes = mutableSetOf()
        board = Board(9, cells)

        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.cells)
        isTakingNotesLiveData.postValue(isTakingNotes)
    }


    fun handleInput(
        number: Int,
        cells: MutableList<Cell>?,
        textView: TextView,
        chronometer: Chronometer,
        prefs: SharedPreferences,

    ) {
        if (selectedRow == -1 || selectedCol == -1) return
        val cell = board.getCell(selectedRow, selectedCol)
        if (cell.isStartingCell) return

        if (isTakingNotes) {
            if (cell.notes.contains(number)) {
                cell.notes.remove(number)
            } else {
                cell.notes.add(number)
            }
            highlightedKeysLiveData.postValue(cell.notes)
        } else {
            cell.value = number
            checkConflictResolution(selectedRow,selectedCol,cells)
        }
        deleteConflict(cells);
        checkVictory(cells,textView,chronometer,prefs)
        cellsLiveData.postValue(board.cells)
    }

    fun updateSelectedCell(row: Int, col: Int) {
        val cell = board.getCell(row, col)
        if (!cell.isStartingCell) {
            selectedRow = row
            selectedCol = col
            selectedCellLiveData.postValue(Pair(row, col))

            if (isTakingNotes) {
                highlightedKeysLiveData.postValue(cell.notes)
            }
        }

    }

    fun changeNoteTakingState() {
        isTakingNotes = !isTakingNotes
        isTakingNotesLiveData.postValue(isTakingNotes)

        val curNotes = if (isTakingNotes) {
            board.getCell(selectedRow, selectedCol).notes
        } else {
            setOf<Int>()
        }
        highlightedKeysLiveData.postValue(curNotes)
    }

    fun delete(cells: MutableList<Cell>?,textView: TextView,chronometer: Chronometer,prefs: SharedPreferences) {

        val cell = board.getCell(selectedRow, selectedCol)
        if (isTakingNotes) {
            cell.notes.clear()
            highlightedKeysLiveData.postValue(setOf())
        } else {
            cell.value = 0
        }
        deleteConflict(cells);
        checkVictory(cells,textView,chronometer,prefs)
        cellsLiveData.postValue(board.cells)

    }
    fun deleteConflict(cells: MutableList<Cell>?){
        cells?.forEach {
            if(!checkConflictResolution(it.row,it.col,cells))cells[it.row * 9 + it.col].isConflict=false;
        }
    }

   fun checkConflictResolution(row:Int,col:Int,cells:MutableList<Cell>?):Boolean {
        val selectedCell:Cell= cells?.get(row * 9 + col)!!
        var hasConflict=false;
        cells?.forEach {

            val r = it.row
            val c = it.col

            if ((r == row || c == col) && !(r == row && c == col)&&it.value!=0) {
                if (it.value == selectedCell.value) {
                    if (it.isStartingCell == false) {
                        it.isConflict = true
                        hasConflict=true;
                    }
                    selectedCell.isConflict = true;
                    hasConflict=true;
                }

            } else if ((r / sqrtSize == row / sqrtSize && c / sqrtSize == col / sqrtSize) && !(r == row && c == col)&&it.value!=0) {
                if (it.value == selectedCell.value) {
                    if (it.isStartingCell == false) {
                        it.isConflict = true
                        hasConflict=true;
                    }
                    selectedCell.isConflict = true
                    hasConflict=true;
                }
            }
        }
        cells!!.set(row * 9 + col, selectedCell)
        return hasConflict;
    }
    private fun checkVictory(cells:MutableList<Cell>?,textView: TextView, chronometer: Chronometer, prefs: SharedPreferences)
    {
        var lose=false
        cells?.forEach {
            if (it.isConflict||checkConflictResolution(it.row,it.col,cells)||it.value==0) lose=true
        }
        if(!lose) {
          textView.setText("Victory!")
            chronometer.stop()
            val e:SharedPreferences.Editor = prefs.edit()
            e.putLong((SystemClock.elapsedRealtime() - chronometer.base).toString(),(SystemClock.elapsedRealtime() - chronometer.base))
            e.apply()
            var times:MutableMap<String,Long> = prefs.all as MutableMap<String, Long>

            var timesTemp:MutableList<Long> = mutableListOf()

            for ((key,value) in times)
                timesTemp.add(value)
            timesTemp=timesTemp.toSortedSet().toMutableList()
            while(timesTemp.size>8)
                timesTemp.removeLast()
            e.clear()
            for (value in timesTemp)
                e.putLong(value.toString(),value)
        }
    }

    private fun transposing(cells:MutableList<Cell>?) {
        for (i in 0..size-1) {
            for (j in 0..size-1) {
                if (j > i) {
                    val a = cells!![i*size + j].value
                    cells[i*size + j].value = cells[j * size + i].value
                    cells[j * size + i].value = a
                }
            }
        }
    }

    private fun swapRowsSmall(cells:MutableList<Cell>?) {
        val area = Random.nextInt(0, 3)
        val line1 = Random.nextInt(0, 3)
        var line2 = Random.nextInt(0, 3)
        val n1 = area * sqrtSize + line1
        while (line1 == line2) {
            line2 = Random.nextInt(0, 3)
        }
        val n2 = area * sqrtSize + line2

        for (i in 0..size-1) {
            val a = cells!![n1*size+i].value
            cells[n1*size+i].value = cells[n2*size+i].value
            cells[n2*size+i].value = a
        }
    }

    private fun swapColumnsSmall(cells:MutableList<Cell>?) {
        transposing(cells)
        swapRowsSmall(cells)
        transposing(cells)
    }

    private fun swapRowsArea(cells:MutableList<Cell>?) {
        val area1 = Random.nextInt(0, 3)
        var area2 = Random.nextInt(0, 3)

        while (area1 == area2) {
            area2 = Random.nextInt(0, 3)
        }

        for (i in 0..sqrtSize-1) {
            for (j in 0..size-1) {
                val a = cells!![(area1*sqrtSize+i)*size+j].value
                cells[(area1*sqrtSize+i)*size+j].value = cells[(area2*sqrtSize+i)*size+j].value
                cells[(area2*sqrtSize+i)*size+j].value = a
            }
        }

    }

    private fun swapColumnsArea(cells:MutableList<Cell>?) {
        transposing(cells)
        swapRowsArea(cells)
        transposing(cells)
    }

    fun generating(cells:MutableList<Cell>?) {
        for(i in 0..80)
            cells!![i].isStartingCell=false
        for (i in 0..size-1) {
            for (j in 0..size-1) {
                cells!![i * size + j].value = (i*sqrtSize + i/sqrtSize + j) % (size) + 1
            }
        }
        for (i in 0..500) {
            var choise: Int = Random.nextInt(0, 5)
            when (choise) {
                0 -> transposing(cells)
                1 -> swapRowsSmall(cells)
                2 -> swapColumnsSmall(cells)
                3 -> swapRowsArea(cells)
                4 -> swapColumnsArea(cells)
            }
        }
        var n: Int = 45
        for (i in 0..n) {
            var deleted: Int = Random.nextInt(0, 81)
            if (cells!![deleted].value == 0) n++
            else
                cells[deleted].value = 0;


        }
        for (i in 0..80) if (cells!![i].value != 0) cells[i].isStartingCell = true

        isTakingNotesLiveData.postValue(isTakingNotes)

    }


}