package no.bcdc.cdigenerator.lookups;

import java.time.LocalDate;

/**
 * CSR reference data for lookup checks
 * @author Steve Jones
 *
 */
public class CSREntry implements Comparable<CSREntry> {

	/**
	 * The cruise start date
	 */
	private LocalDate startDate;
	
	/**
	 * The cruise end date
	 */
	private LocalDate endDate;
	
	/**
	 * The CSR reference
	 */
	private String csrReference;
	
	/**
	 * Simple constructor
	 * @param startDate The cruise start date
	 * @param endDate The cruise end date
	 * @param csrReference The CSR reference
	 */
	protected CSREntry(LocalDate startDate, LocalDate endDate, String csrReference) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.csrReference = csrReference;
	}
	
	/**
	 * See if this entry encompasses a specified date
	 * @param date The date to be checked
	 * @return {@code true} if the date is within this entry's time period; {@code false} if it is not.
	 */
	protected boolean encompassesDate(LocalDate date) {
		return (startDate.compareTo(date) <= 0 && endDate.compareTo(date) > 0);
	}
	
	/**
	 * Get the CSR reference
	 * @return The CSR reference
	 */
	protected String getCsrReference() {
		return csrReference;
	}

	@Override
	public int compareTo(CSREntry o) {
		return startDate.compareTo(o.startDate);
	}
}
