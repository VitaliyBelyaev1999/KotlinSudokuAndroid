package com.example.sudoku.view
import kotlinx.android.synthetic.main. activity_main.*

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sudoku.R
import com.example.sudoku.game.Cell
import com.example.sudoku.view.custom.SudokuBoardView
import com.example.sudoku.viewmodel.PlaySudokuViewModel
import android.content.Intent
import android.content.SharedPreferences
import com.example.sudoku.Best_list
import kotlinx.android.synthetic.main.activity_best_list.*
import java.util.*
import android.widget.Chronometer
import kotlin.system.exitProcess
import android.os.SystemClock
import android.widget.TextView

class MainActivity : AppCompatActivity(), SudokuBoardView.OnTouchListener {

    private lateinit var viewModel: PlaySudokuViewModel
    private lateinit var numberButtons: List<Button>
    private lateinit var chronometer: Chronometer
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

        sudokuBoardView.registerListener(this)

        viewModel = ViewModelProvider(this).get(PlaySudokuViewModel::class.java)
        viewModel.sudokuGame.selectedCellLiveData.observe(this, Observer { updateSelectedCellUI(it) })
        viewModel.sudokuGame.cellsLiveData.observe(this, Observer { updateCells(it as MutableList<Cell>?) })
        viewModel.sudokuGame.isTakingNotesLiveData.observe(this, Observer { updateNoteTakingUI(it) })
        viewModel.sudokuGame.highlightedKeysLiveData.observe(this, Observer { updateHighlitedKeys(it) })

        numberButtons = listOf(oneButton, twoButton, threeButton, fourButton, fiveButton,
            sixButton, sevenButton, eightButton, nineButton)

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener { viewModel.sudokuGame.handleInput(index + 1, sudokuBoardView.cells, textVictory, chronometer,prefs)  }
        }
        chronometer = findViewById(R.id.chronometer)
        chronometer.setOnChronometerTickListener {
            val elapsedMillis: Long = (SystemClock.elapsedRealtime() - chronometer.base)
        }
        chronometer.start()
        notesButton.setOnClickListener { viewModel.sudokuGame.changeNoteTakingState() }
        deleteButton.setOnClickListener { viewModel.sudokuGame.delete(sudokuBoardView.cells, textVictory, chronometer,prefs)}
        exitButton.setOnClickListener(){
           exitProcess(-1)
            }
        prefs = getSharedPreferences("bestlist", Context.MODE_PRIVATE)
        bestResultsButton.setOnClickListener(){
            val intent = Intent(this, Best_list::class.java).apply {
            }
            startActivity(intent)

        }
        newGameButton.setOnClickListener(){
            viewModel.sudokuGame.generating(sudokuBoardView.cells)
            chronometer.setBase(SystemClock.elapsedRealtime())

            viewModel.sudokuGame.cellsLiveData.observe(this, Observer { updateCells(it as MutableList<Cell>?) })

        }
    }

    private fun updateCells(cells: MutableList<Cell>?) = cells?.let {
        sudokuBoardView.updateCells(cells)
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) = cell?.let {
        sudokuBoardView.updateSelectedCellUI(cell.first, cell.second)
    }


    private fun updateNoteTakingUI(isNoteTaking: Boolean?) = isNoteTaking?.let {
        val color = if (it) ContextCompat.getColor(this, R.color.colorPrimary) else Color.LTGRAY
    }

    private fun updateHighlitedKeys(set: Set<Int>?) = set?.let {
        numberButtons.forEachIndexed { index, button ->
            val color = if (set.contains(index + 1)) ContextCompat.getColor(this, R.color.colorPrimary) else Color.LTGRAY
        }
    }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.sudokuGame.updateSelectedCell(row, col)
    }


}


