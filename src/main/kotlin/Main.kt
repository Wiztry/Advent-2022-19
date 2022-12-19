import java.io.File
import java.util.*

data class BluePrint(
    val oreRobotCostOre: Int,
    val clayRobotCostOre: Int,
    val obsidianRobotCostOre: Int,
    val obsidianRobotCostClay: Int,
    val geodeRobotCostOre: Int,
    val geodeRobotCostObsidian: Int
)

data class RobotState(
    val oreRobots: Int,
    val ore: Int,
    val clayRobots: Int,
    val clay: Int,
    val obsidianRobots: Int,
    val obsidian: Int,
    val geodeRobots: Int,
    val geode: Int,
    val timeLeft: Int
)

fun main() {
    val inputFile = File("src/main/kotlin/input3")
    val inputStrings = inputFile.readLines()
    val bluePrintList = inputStrings.map(::consumeString)
    val start = System.currentTimeMillis()
    val geodeCounts = bluePrintList.map{
        BFSRobots(it,24)
    }
    val qualitySum = geodeCounts.mapIndexed { index, i ->
        i*(index+1)
    }.sum()
    val part1 = System.currentTimeMillis() - start
    val geodeCounts2 = bluePrintList.take(3).map{
        BFSRobots(it, 32)
    }
    val result = geodeCounts2[0]*geodeCounts2[1]*geodeCounts2[2]
    val part2 = System.currentTimeMillis() - part1 - start
    println("Product of 3 first blueprints: $result")
    println("Sum of Quality Levels: $qualitySum")
    println("Time for part 1: $part1 ms")
    println("Time for part 2: $part2 ms")
}

fun BFSRobots(bluePrint: BluePrint, timeLimit: Int): Int {
    val stack = Stack<RobotState>()
    stack.add(RobotState(1, 0, 0, 0, 0, 0, 0, 0, timeLimit))
    var bestGeodeCount = 0
    while (stack.isNotEmpty()) {
        val currentState = stack.pop()
        if(calculateMaxGeodeNumberPossible(currentState) >= bestGeodeCount && (calculateMaxObsidianNumberPossible(currentState) >= bluePrint.geodeRobotCostObsidian || currentState.obsidianRobots != 0)) {
            val newStates = computeNewStates(currentState, bluePrint)
            if (newStates.isEmpty()) {
                if (currentState.geode > bestGeodeCount) {
                    bestGeodeCount = currentState.geode
                }
            }
            newStates.forEach { stack.push(it) }
        }
    }
    return bestGeodeCount
}

fun calculateMaxGeodeNumberPossible(state : RobotState): Int{
    val geodeRobots = state.geodeRobots
    val timeLeft = state.timeLeft
    var numberOfGeodesPossible = state.geode
    for(i in 0 until timeLeft){
        numberOfGeodesPossible += geodeRobots + i
    }
    return numberOfGeodesPossible
}

fun calculateMaxObsidianNumberPossible(state : RobotState): Int{
    val obsidianRobots = state.obsidianRobots
    val timeLeft = state.timeLeft
    var numberOfObsidianPossible = state.obsidian
    for(i in 0 until timeLeft){
        numberOfObsidianPossible += obsidianRobots + i
    }
    return numberOfObsidianPossible
}

fun computeNewStates(state: RobotState, bluePrint: BluePrint): List<RobotState> {
    val canAffordOreRobot = state.ore >= bluePrint.oreRobotCostOre
    val canAffordClayRobot = state.ore >= bluePrint.clayRobotCostOre
    val canAffordObsidianRobot =
        state.ore >= bluePrint.obsidianRobotCostOre && state.clay >= bluePrint.obsidianRobotCostClay
    val canAffordGeodeRobot =
        state.ore >= bluePrint.geodeRobotCostOre && state.obsidian >= bluePrint.geodeRobotCostObsidian
    val newOreCount = state.ore + state.oreRobots
    val newClayCount = state.clay + state.clayRobots
    val newObsidianCount = state.obsidian + state.obsidianRobots
    val newGeodeCount = state.geode + state.geodeRobots
    val newStates = mutableListOf<RobotState>()
    val maxOreCost = maxOf<Int>(bluePrint.oreRobotCostOre, bluePrint.clayRobotCostOre, bluePrint.obsidianRobotCostOre, bluePrint.geodeRobotCostOre)
    val maxClayCost = bluePrint.obsidianRobotCostClay
    val maxObsidianCost = bluePrint.geodeRobotCostObsidian
    if (state.timeLeft > 0) {
        if (canAffordGeodeRobot) {
            newStates.add(
                RobotState(
                    state.oreRobots,
                    newOreCount - bluePrint.geodeRobotCostOre,
                    state.clayRobots,
                    newClayCount,
                    state.obsidianRobots,
                    newObsidianCount - bluePrint.geodeRobotCostObsidian,
                    state.geodeRobots + 1,
                    newGeodeCount,
                    state.timeLeft - 1
                )
            )
            return newStates
        }
        if (canAffordObsidianRobot && maxObsidianCost > state.obsidianRobots) {
            newStates.add(
                RobotState(
                    state.oreRobots,
                    newOreCount - bluePrint.obsidianRobotCostOre,
                    state.clayRobots,
                    newClayCount - bluePrint.obsidianRobotCostClay,
                    state.obsidianRobots + 1,
                    newObsidianCount,
                    state.geodeRobots,
                    newGeodeCount,
                    state.timeLeft - 1
                )
            )
        }
        if (canAffordClayRobot && maxClayCost > state.clayRobots) {
            newStates.add(
                RobotState(
                    state.oreRobots,
                    newOreCount - bluePrint.clayRobotCostOre,
                    state.clayRobots + 1,
                    newClayCount,
                    state.obsidianRobots,
                    newObsidianCount,
                    state.geodeRobots,
                    newGeodeCount,
                    state.timeLeft - 1
                )
            )
        }
        if (canAffordOreRobot && maxOreCost > state.oreRobots) {
            newStates.add(
                RobotState(
                    state.oreRobots + 1,
                    newOreCount - bluePrint.oreRobotCostOre,
                    state.clayRobots,
                    newClayCount,
                    state.obsidianRobots,
                    newObsidianCount,
                    state.geodeRobots,
                    newGeodeCount,
                    state.timeLeft - 1
                )
            )
        }
        if(state.ore < maxOreCost) {
            newStates.add(
                RobotState(
                    state.oreRobots,
                    newOreCount,
                    state.clayRobots,
                    newClayCount,
                    state.obsidianRobots,
                    newObsidianCount,
                    state.geodeRobots,
                    newGeodeCount,
                    state.timeLeft - 1
                )
            )
        }
    }
    return newStates
}

fun consumeString(s: String): BluePrint {
    val bluePrintParts = s.split(".")
    val oreRobotParts = bluePrintParts[0].split(" ")
    val oreRobotCostOre = oreRobotParts[6].toInt()
    val clayRobotParts = bluePrintParts[1].split(" ")
    val clayRobotCostOre = clayRobotParts[5].toInt()
    val obsidianRobotParts = bluePrintParts[2].split(" ")
    val obsidianRobotCostOre = obsidianRobotParts[5].toInt()
    val obsidianRobotCostClay = obsidianRobotParts[8].toInt()
    val geodeRobotParts = bluePrintParts[3].split(" ")
    val geodeRobotCostOre = geodeRobotParts[5].toInt()
    val geodeRobotCostObsidian = geodeRobotParts[8].toInt()
    return BluePrint(
        oreRobotCostOre,
        clayRobotCostOre,
        obsidianRobotCostOre,
        obsidianRobotCostClay,
        geodeRobotCostOre,
        geodeRobotCostObsidian
    )
}
