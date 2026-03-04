package eu.espcaa.boardingpassscanner.parser

import java.time.LocalDate

// interesting :p https://www.iata.org/contentassets/1dccc9ed041b4f3bbdcf8ee8682e75c4/2021_03_02-bcbp-implementation-guide-version-7-.pdf

data class BoardingPass(
    val numberOfLegs: Int,       // index 1 :3
    val passengerName: String,   // mandatory, 20 chars
    val pnrCode: String,         // mandatory, 7 chars
    val issueDate: LocalDate?,          // derived from conditional data, may be null if not present or parsing fails
    val issueYear: Int?,           // derived from issueDate, may be null if issueDate is null
    val legs: List<Leg>
)

data class Leg(
    val from: String,            // mandatory: 3 chars ( iata airport code )
    val to: String,              // mandatory: 3 chars ( iata airport code )
    val carrier: String,         // mandatory: 3 chars ( iata airline code )
    val flightNumber: String,    // mandatory: 5 chars ( right justified, zero filled )
    val flightJulian: String,          // mandatory: 3-digit julian date
    val flightDate: LocalDate?,          // derived from flightJulian, may be null if parsing fails
    val seat: String,            // mandatory : 4 chars ( right justified, space filled )
    val sequenceNumber: String,  // mandatory: 5 chars ( right justified, zero filled )
    val compartmentCode: String,     // mandatory: 1 char cabin class
    val isEticket: Boolean       // mandatory: 1 char ( 'E' for e-ticket, space for paper ticket)
)

data class Error(
    val message: String,
    val details: String? = null
)

data class BCBPParseResult(
    val boardingPass: BoardingPass?,
    val errors: List<Error>
)


fun ParseIATADate(julianDate: String, currentYear: Int = LocalDate.now().year): LocalDate? {
    return try {
        if (julianDate.length == 3) {
            LocalDate.ofYearDay(currentYear, julianDate.toInt())
        } else if (julianDate.length == 4) {
            val year = julianDate.substring(0, 1).toInt() + (currentYear / 10) * 10
            val dayOfYear = julianDate.substring(1).toInt()
            LocalDate.ofYearDay(year, dayOfYear)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

fun ParseBCBP(rawData: String): BCBPParseResult {
    val errors = mutableListOf<Error>()
    val legs = mutableListOf<Leg>()

    try {


        if (rawData.length < 60 || rawData[0] != 'M') {
            errors.add(
                Error(
                    "Invalid format",
                    "Not enough characters or does not start with 'M', rawData: $rawData"
                )
            )
            return BCBPParseResult(null, errors)
        }

        val numberOfLegs = rawData[1].toString().toIntOrNull()?.takeIf { it in 1..4 } ?: run {
            errors.add(
                Error(
                    "Invalid number of legs",
                    "Expected a digit between 1 and 4 at position 1, got '${rawData[1]}'"
                )
            )
            return BCBPParseResult(null, errors)
        }

        val passengerName = rawData.substring(2, 22).trim()
        val isEticket = rawData[22] == 'E'
        val pnrCode = rawData.substring(23, 30).trim()

        var pointer = 30

        var julianIssueDate = ""
        var issueDate: LocalDate? = null

        repeat(numberOfLegs) { legIndex ->
            android.util.Log.d("ScanScreen", "Leg $legIndex start pointer: $pointer")

            val from = rawData.substring(pointer, pointer + 3).trim().also { pointer += 3 }
            val to = rawData.substring(pointer, pointer + 3).trim().also { pointer += 3 }
            android.util.Log.d("ScanScreen", "Leg $legIndex from: $from to: $to pointer: $pointer")

            val carrier = rawData.substring(pointer, pointer + 3).trim().also { pointer += 3 }
            val flightNumber =
                rawData.substring(pointer, pointer + 5).trim().also { pointer += 5 }
            val flightDate =
                rawData.substring(pointer, pointer + 3).trim().also { pointer += 3 }
            val compartmentCode = rawData[pointer].toString().also { pointer += 1 }
            val seat = rawData.substring(pointer, pointer + 4).trim().also { pointer += 4 }
            val sequenceNumber =
                rawData.substring(pointer, pointer + 5).trim().also { pointer += 5 }
            pointer += 1

            val conditionalSize =
                rawData.substring(pointer, pointer + 2).toInt(16).also { pointer += 2 }
            val conditionalStart = pointer

            android.util.Log.d(
                "ScanScreen",
                "Leg $legIndex conditionalSize: $conditionalSize conditionalStart: $conditionalStart rawData around pointer: '${
                    rawData.substring(
                        pointer - 2,
                        minOf(pointer + 20, rawData.length)
                    )
                }'"
            )

            if (conditionalSize > 0 && legIndex == 0) {

                pointer += 4
                julianIssueDate = rawData.substring(pointer, pointer + 4)
                issueDate = ParseIATADate(julianIssueDate)
            }

            pointer = conditionalStart + conditionalSize

            legs.add(
                Leg(
                    from,
                    to,
                    carrier,
                    flightNumber,
                    flightDate,
                    ParseIATADate(flightDate, issueDate?.year ?: LocalDate.now().year),
                    seat,
                    sequenceNumber,
                    compartmentCode,
                    isEticket
                )
            )
        }


        val boardingPass = BoardingPass(
            numberOfLegs,
            passengerName,
            pnrCode,
            issueDate,
            issueDate?.year,
            legs
        )
        return BCBPParseResult(boardingPass, errors)
    } catch (e: Exception) {
        errors.add(Error("Parsing error", e.message ?: "Unknown error"))
        return BCBPParseResult(null, errors)
    }
}
