package eu.espcaa.boardingpassscanner.parser

import java.time.LocalDate

// interesting :p https://www.iata.org/contentassets/1dccc9ed041b4f3bbdcf8ee8682e75c4/2021_03_02-bcbp-implementation-guide-version-7-.pdf

data class JulianBoardingPass(
    val numberOfLegs: Int,       // index 1 :3
    val passengerName: String,   // mandatory, 20 chars
    val pnrCode: String,         // mandatory, 7 chars
    val legs: List<JulianLeg>,
    val isEticket: Boolean       // mandatory: 1 char ( 'E' for e-ticket, space for paper ticket)
)

data class JulianLeg(
    val from: String,            // mandatory: 3 chars ( iata airport code )
    val to: String,              // mandatory: 3 chars ( iata airport code )
    val carrier: String,         // mandatory: 3 chars ( iata airline code )
    val flightNumber: String,    // mandatory: 5 chars ( right justified, zero filled )
    val flightJulian: String,          // mandatory: 3-digit julian date
    val flightDateJulian: String, // mandatory: 3-digit julian date
    val seat: String,            // mandatory : 4 chars ( right justified, space filled )
    val sequenceNumber: String,  // mandatory: 5 chars ( right justified, zero filled )
    val compartmentCode: String,     // mandatory: 1 char cabin class
)

data class BoardingPass(
    val numberOfLegs: Int,       // index 1 :3
    val passengerName: String,   // mandatory, 20 chars
    val pnrCode: String,         // mandatory, 7 chars
    val issueYear: Int? = null,
    val legs: List<Leg>
)

data class Leg(
    val from: String,            // mandatory: 3 chars ( iata airport code )
    val to: String,              // mandatory: 3 chars ( iata airport code )
    val carrier: String,         // mandatory: 3 chars ( iata airline code )
    val flightNumber: String,    // mandatory: 5 chars ( right justified, zero filled )
    val flightJulian: String,          // mandatory: 3-digit julian date
    val flightDate: LocalDate?,       // derived from flightJulian, may be null if flightJulian is null or invalid
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
    val boardingPass: JulianBoardingPass?,
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
    val legs = mutableListOf<JulianLeg>()

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
        val firstFlightDeparture = rawData.substring(30, 33)
        val firstFlightArrival = rawData.substring(33, 36)
        val firstFlightCarrier = rawData.substring(36, 39).trim()
        val firstFlightNumber = rawData.substring(39, 44).trim()
        val firstFlightJulian = rawData.substring(44, 47)
        val compartmentCode = rawData[47].toString()
        val firstFlightSeat = rawData.substring(48, 52).trim()
        val firstFlightSequence = rawData.substring(51, 56).trim()

        legs.add(
            JulianLeg(
                from = firstFlightDeparture,
                to = firstFlightArrival,
                carrier = firstFlightCarrier,
                flightNumber = firstFlightNumber,
                flightJulian = firstFlightJulian,
                flightDateJulian = firstFlightJulian,
                seat = firstFlightSeat,
                sequenceNumber = firstFlightSequence,
                compartmentCode = compartmentCode,
            )
        )

        var pointer = 58

        repeat(numberOfLegs - 1) { i ->
            val hex = rawData.substring(pointer, pointer + 2)
            val legLength = hex.toIntOrNull(16) ?: run {
                errors.add(
                    Error(
                        "Invalid leg length",
                        "Expected a 2-digit hex number at position $pointer, got '$hex'"
                    )
                )
                return BCBPParseResult(null, errors)
            }

            // move ahead of the hex length field
            pointer += 2

            // move ahead of the leg data + skip the pnr code for now (7 chars)
            pointer += legLength + 7

            val legDeparture = rawData.substring(pointer, pointer + 3)
            val legArrival = rawData.substring(pointer + 3, pointer + 6)
            val legCarrier = rawData.substring(pointer + 6, pointer + 9).trim()
            val legFlightNumber = rawData.substring(pointer + 9, pointer + 14).trim()
            val legFlightJulian = rawData.substring(pointer + 14, pointer + 17)
            val legCompartmentCode = rawData[pointer + 17].toString()
            val legSeat = rawData.substring(pointer + 18, pointer + 22).trim()
            val legSequence = rawData.substring(pointer + 21, pointer + 26).trim()

            // go to next leg :)
            pointer += 28

            legs.add(
                JulianLeg(
                    from = legDeparture,
                    to = legArrival,
                    carrier = legCarrier,
                    flightNumber = legFlightNumber,
                    flightJulian = legFlightJulian,
                    flightDateJulian = legFlightJulian,
                    seat = legSeat,
                    sequenceNumber = legSequence,
                    compartmentCode = legCompartmentCode,
                )
            )
        }

        val boardingPass = JulianBoardingPass(
            numberOfLegs = numberOfLegs,
            passengerName = passengerName,
            pnrCode = pnrCode,
            legs = legs,
            isEticket = isEticket
        )
        return BCBPParseResult(boardingPass, errors)
    } catch (e: Exception) {
        errors.add(Error("Parsing error", e.message ?: "Unknown error"))
        return BCBPParseResult(null, errors)
    }
}

fun ConvertToBoardingPass(
    julianPass: JulianBoardingPass,
    year: Int? = LocalDate.now().year
): BoardingPass {
    val legs = julianPass.legs.map { leg ->
        Leg(
            from = leg.from,
            to = leg.to,
            carrier = leg.carrier,
            flightNumber = leg.flightNumber,
            flightJulian = leg.flightJulian,
            flightDate = ParseIATADate(leg.flightJulian, year ?: LocalDate.now().year),
            seat = leg.seat,
            sequenceNumber = leg.sequenceNumber,
            compartmentCode = leg.compartmentCode,
            isEticket = julianPass.isEticket
        )
    }

    return BoardingPass(
        numberOfLegs = julianPass.numberOfLegs,
        passengerName = julianPass.passengerName,
        pnrCode = julianPass.pnrCode,
        issueYear = year,
        legs = legs
    )
}