/*
 * NOTE: This is added by intellij IDE. Disregard this copyright if there is another copyright later in the file.
 *
 * MIT License
 *
 * Copyright (C) 2016 Will (n9Mtq4) Bresnahan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.n9mtq4.fourhasfourletters

import stackoverflow.yanickrochon.numtostring.NumberToWords
import kotlin.concurrent.thread

/**
 * Created by will on 9/10/2016 at 9:34 PM.
 * 
 * Searches for chains of spelled out numbers.
 * More info here: https://www.youtube.com/watch?v=LYKn0yUTIU4
 * 
 * With help from http://stackoverflow.com/a/3911982/5196460
 * 
 * @author Will "n9Mtq4" Bresnahan
 */

private val toWord = NumberToWords.DefaultProcessor()

fun main(args: Array<String>) {
	
	if (args.size == 4) {
		
		val numRanges: List<Long>
		try {
			numRanges = args.map { it.toLong() }
		}catch (e: NumberFormatException) {
			println("You didn't enter all numbers!")
			printHelp()
			return
		}
		
		println()
		println("Starting searching for number word chains.")
		println("From ${numRanges[0]} to ${numRanges[1]}")
		println("with ${numRanges[2]} threads")
		println("printing anything with a chain longer than ${numRanges[3]}")
		println()
		
		testMultithreaded(numRanges[0], numRanges[1], numRanges[2].toInt(), numRanges[3].toInt())
		
	}else {
		printHelp()
	}
	
}

private fun printHelp() {
	println("Run with the args: min max threads threshold")
}

/*
* To set how many threads / cpu cores to use add "threads = num" into the parenthesises.
* By default it uses all your cpu cores.
* 
* To set the min range of the numbers to test add "min = num" into the parenthesises.
* By default it uses 0.
* 
* To set the max range of the numbers to test add "max = numL" into the parenthesises.
* By default it uses Long.MAX_VALUE which is 2^63 - 1 or 9223372036854775807
* 
* To set the printing threshold add "threshold = num" into the parenthesises.
* By default it uses 8, 7 is too easy
* */
fun testMultithreaded(min: Long = 0, max: Long = Long.MAX_VALUE, threads: Int = Runtime.getRuntime().availableProcessors(), threshold: Int = 7) {
	
	for (core in 0..threads - 1) {
		
		val delta = (max - min) / threads
		val adjustedMin = min + (core * delta)
		// if the range is not even divisible by the thread count, it will skip range % threads numbers -> adding the max onto the last thread prevents this
		val adjustedMax = if (core == threads - 1) max else min + ((core + 1) * delta)
		
		thread(start = true) {
			runRange(adjustedMin, adjustedMax, threshold = threshold, threadNum = core)
		}
		
		println("Waiting 1 second(s) to stagger thread starts...")
		Thread.sleep(1000)
		
	}
	
}

fun runRange(l1: Long, l2: Long, threshold: Int = 7, threadNum: Int = -1) {
	println("${if (threadNum != -1) "[Thread $threadNum] " else ""}Starting thread testing: $l1 -> $l2")
	for (i in l1..l2) {
		val numRuns = checkNumFast(i)
		if (numRuns >= threshold){
			// we got one!
			println("${if (threadNum != -1) "[Thread $threadNum] " else ""}The number $i has a chain of $numRuns!")
			// an iteration is so fast, we should just re-run it with the slower printing function
			checkNumPrinting(i, threadNum = threadNum)
		}
	}
}

tailrec fun checkNumFast(num: Long, chain: Int = 1): Int {
	
	val name = toWord.getName(num)
	val size = name.replace(" ", "").replace("-", "").length // no spaces or dashes.length
	
	if (num == 4L) return chain
	
	return checkNumFast(size.toLong(), chain + 1)
	
}

fun checkNumPrinting(num: Long, chain: Int = 1, threadNum: Int = -1): Int {
	
	val numRuns = checkNumPrintingRecursive(num, chain, threadNum = threadNum)
	println("${if (threadNum != -1) "[Thread $threadNum] " else ""}$numRuns\t4\tfour\t4")
	return numRuns
	
}

private tailrec fun checkNumPrintingRecursive(num: Long, chain: Int, threadNum: Int = -1): Int {
	
	val threadName = if (threadNum != -1) "[Thread $threadNum] " else ""
	
	val name = toWord.getName(num)
	val size = name.replace(" ", "").replace("-", "").length // no spaces or dashes.length
	
	if (num == 4L) return chain
	println("$threadName$chain\t$num\t$name\t$size")
	
	return checkNumPrintingRecursive(size.toLong(), chain + 1, threadNum = threadNum)
}
