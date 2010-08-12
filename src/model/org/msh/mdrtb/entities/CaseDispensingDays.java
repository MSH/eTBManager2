package org.msh.mdrtb.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Store information about daily dispensing for an specific month of a case 
 * @author Ricardo Memoria
 *
 */
@Entity
public class CaseDispensingDays {

	@Id
    private Integer id;

	@OneToOne(mappedBy="dispensingDays", cascade={CascadeType.ALL})
	private CaseDispensing caseDispensing;
	
	private boolean day1;
	private boolean day2;
	private boolean day3;
	private boolean day4;
	private boolean day5;
	private boolean day6;
	private boolean day7;
	private boolean day8;
	private boolean day9;
	private boolean day10;
	private boolean day11;
	private boolean day12;
	private boolean day13;
	private boolean day14;
	private boolean day15;
	private boolean day16;
	private boolean day17;
	private boolean day18;
	private boolean day19;
	private boolean day20;
	private boolean day21;
	private boolean day22;
	private boolean day23;
	private boolean day24;
	private boolean day25;
	private boolean day26;
	private boolean day27;
	private boolean day28;
	private boolean day29;
	private boolean day30;
	private boolean day31;
	
	public int getNumDispensingDays() {
		int res = 0;
		for (int i = 1; i <= 31; i++)
			if (isDay(i))
				res++;
		return res;
	}
	
	public boolean isDay(int day) {
		try {
			return (Boolean)PropertyUtils.getProperty(this, "day" + Integer.toString(day));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void setDay(int day, boolean value) {
		try {
			PropertyUtils.setProperty(this, "day" + Integer.toString(day), value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the day1
	 */
	public boolean isDay1() {
		return day1;
	}
	/**
	 * @param day1 the day1 to set
	 */
	public void setDay1(boolean day1) {
		this.day1 = day1;
	}
	/**
	 * @return the day2
	 */
	public boolean isDay2() {
		return day2;
	}
	/**
	 * @param day2 the day2 to set
	 */
	public void setDay2(boolean day2) {
		this.day2 = day2;
	}
	/**
	 * @return the day3
	 */
	public boolean isDay3() {
		return day3;
	}
	/**
	 * @param day3 the day3 to set
	 */
	public void setDay3(boolean day3) {
		this.day3 = day3;
	}
	/**
	 * @return the day4
	 */
	public boolean isDay4() {
		return day4;
	}
	/**
	 * @param day4 the day4 to set
	 */
	public void setDay4(boolean day4) {
		this.day4 = day4;
	}
	/**
	 * @return the day5
	 */
	public boolean isDay5() {
		return day5;
	}
	/**
	 * @param day5 the day5 to set
	 */
	public void setDay5(boolean day5) {
		this.day5 = day5;
	}
	/**
	 * @return the day6
	 */
	public boolean isDay6() {
		return day6;
	}
	/**
	 * @param day6 the day6 to set
	 */
	public void setDay6(boolean day6) {
		this.day6 = day6;
	}
	/**
	 * @return the day7
	 */
	public boolean isDay7() {
		return day7;
	}
	/**
	 * @param day7 the day7 to set
	 */
	public void setDay7(boolean day7) {
		this.day7 = day7;
	}
	/**
	 * @return the day8
	 */
	public boolean isDay8() {
		return day8;
	}
	/**
	 * @param day8 the day8 to set
	 */
	public void setDay8(boolean day8) {
		this.day8 = day8;
	}
	/**
	 * @return the day9
	 */
	public boolean isDay9() {
		return day9;
	}
	/**
	 * @param day9 the day9 to set
	 */
	public void setDay9(boolean day9) {
		this.day9 = day9;
	}
	/**
	 * @return the day10
	 */
	public boolean isDay10() {
		return day10;
	}
	/**
	 * @param day10 the day10 to set
	 */
	public void setDay10(boolean day10) {
		this.day10 = day10;
	}
	/**
	 * @return the day11
	 */
	public boolean isDay11() {
		return day11;
	}
	/**
	 * @param day11 the day11 to set
	 */
	public void setDay11(boolean day11) {
		this.day11 = day11;
	}
	/**
	 * @return the day12
	 */
	public boolean isDay12() {
		return day12;
	}
	/**
	 * @param day12 the day12 to set
	 */
	public void setDay12(boolean day12) {
		this.day12 = day12;
	}
	/**
	 * @return the day13
	 */
	public boolean isDay13() {
		return day13;
	}
	/**
	 * @param day13 the day13 to set
	 */
	public void setDay13(boolean day13) {
		this.day13 = day13;
	}
	/**
	 * @return the day14
	 */
	public boolean isDay14() {
		return day14;
	}
	/**
	 * @param day14 the day14 to set
	 */
	public void setDay14(boolean day14) {
		this.day14 = day14;
	}
	/**
	 * @return the day15
	 */
	public boolean isDay15() {
		return day15;
	}
	/**
	 * @param day15 the day15 to set
	 */
	public void setDay15(boolean day15) {
		this.day15 = day15;
	}
	/**
	 * @return the day16
	 */
	public boolean isDay16() {
		return day16;
	}
	/**
	 * @param day16 the day16 to set
	 */
	public void setDay16(boolean day16) {
		this.day16 = day16;
	}
	/**
	 * @return the day17
	 */
	public boolean isDay17() {
		return day17;
	}
	/**
	 * @param day17 the day17 to set
	 */
	public void setDay17(boolean day17) {
		this.day17 = day17;
	}
	/**
	 * @return the day18
	 */
	public boolean isDay18() {
		return day18;
	}
	/**
	 * @param day18 the day18 to set
	 */
	public void setDay18(boolean day18) {
		this.day18 = day18;
	}
	/**
	 * @return the day19
	 */
	public boolean isDay19() {
		return day19;
	}
	/**
	 * @param day19 the day19 to set
	 */
	public void setDay19(boolean day19) {
		this.day19 = day19;
	}
	/**
	 * @return the day20
	 */
	public boolean isDay20() {
		return day20;
	}
	/**
	 * @param day20 the day20 to set
	 */
	public void setDay20(boolean day20) {
		this.day20 = day20;
	}
	/**
	 * @return the day21
	 */
	public boolean isDay21() {
		return day21;
	}
	/**
	 * @param day21 the day21 to set
	 */
	public void setDay21(boolean day21) {
		this.day21 = day21;
	}
	/**
	 * @return the day22
	 */
	public boolean isDay22() {
		return day22;
	}
	/**
	 * @param day22 the day22 to set
	 */
	public void setDay22(boolean day22) {
		this.day22 = day22;
	}
	/**
	 * @return the day23
	 */
	public boolean isDay23() {
		return day23;
	}
	/**
	 * @param day23 the day23 to set
	 */
	public void setDay23(boolean day23) {
		this.day23 = day23;
	}
	/**
	 * @return the day24
	 */
	public boolean isDay24() {
		return day24;
	}
	/**
	 * @param day24 the day24 to set
	 */
	public void setDay24(boolean day24) {
		this.day24 = day24;
	}
	/**
	 * @return the day25
	 */
	public boolean isDay25() {
		return day25;
	}
	/**
	 * @param day25 the day25 to set
	 */
	public void setDay25(boolean day25) {
		this.day25 = day25;
	}
	/**
	 * @return the day26
	 */
	public boolean isDay26() {
		return day26;
	}
	/**
	 * @param day26 the day26 to set
	 */
	public void setDay26(boolean day26) {
		this.day26 = day26;
	}
	/**
	 * @return the day27
	 */
	public boolean isDay27() {
		return day27;
	}
	/**
	 * @param day27 the day27 to set
	 */
	public void setDay27(boolean day27) {
		this.day27 = day27;
	}
	/**
	 * @return the day28
	 */
	public boolean isDay28() {
		return day28;
	}
	/**
	 * @param day28 the day28 to set
	 */
	public void setDay28(boolean day28) {
		this.day28 = day28;
	}
	/**
	 * @return the day29
	 */
	public boolean isDay29() {
		return day29;
	}
	/**
	 * @param day29 the day29 to set
	 */
	public void setDay29(boolean day29) {
		this.day29 = day29;
	}
	/**
	 * @return the day30
	 */
	public boolean isDay30() {
		return day30;
	}
	/**
	 * @param day30 the day30 to set
	 */
	public void setDay30(boolean day30) {
		this.day30 = day30;
	}
	/**
	 * @return the day31
	 */
	public boolean isDay31() {
		return day31;
	}
	/**
	 * @param day31 the day31 to set
	 */
	public void setDay31(boolean day31) {
		this.day31 = day31;
	}

	/**
	 * @return the caseDispensing
	 */
	public CaseDispensing getCaseDispensing() {
		return caseDispensing;
	}

	/**
	 * @param caseDispensing the caseDispensing to set
	 */
	public void setCaseDispensing(CaseDispensing caseDispensing) {
		this.caseDispensing = caseDispensing;
	}
}
