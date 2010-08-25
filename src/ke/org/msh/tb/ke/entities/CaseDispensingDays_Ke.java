package org.msh.tb.ke.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.apache.commons.beanutils.PropertyUtils;
import org.msh.tb.ke.entities.enums.Dot;

/**
 * Store information about daily dispensing for an specific month of a case 
 * @author Ricardo Memoria
 *
 */
@Entity
public class CaseDispensingDays_Ke {

	@Id
    private Integer id;

	@OneToOne(mappedBy="dispensingDays", cascade={CascadeType.ALL})
	private CaseDispensing_Ke caseDispensing;
	
	private Dot day1;
	private Dot day2;
	private Dot day3;
	private Dot day4;
	private Dot day5;
	private Dot day6;
	private Dot day7;
	private Dot day8;
	private Dot day9;
	private Dot day10;
	private Dot day11;
	private Dot day12;
	private Dot day13;
	private Dot day14;
	private Dot day15;
	private Dot day16;
	private Dot day17;
	private Dot day18;
	private Dot day19;
	private Dot day20;
	private Dot day21;
	private Dot day22;
	private Dot day23;
	private Dot day24;
	private Dot day25;
	private Dot day26;
	private Dot day27;
	private Dot day28;
	private Dot day29;
	private Dot day30;
	private Dot day31;
	
	public int getNumDispensingDays() {
		int res = 0;
		for (int i = 1; i <= 31; i++)
			if (getDay(i)!=null)
				res++;
		return res;
	}
	
	public Dot getDay(int day) {
		try {
			return (Dot)PropertyUtils.getProperty(this, "day" + Integer.toString(day));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public void setDay(int day, Dot value) {
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
	public Dot getDay1() {
		return day1;
	}
	/**
	 * @param day1 the day1 to set
	 */
	public void setDay1(Dot day1) {
		this.day1 = day1;
	}
	/**
	 * @return the day2
	 */
	public Dot getDay2() {
		return day2;
	}
	/**
	 * @param day2 the day2 to set
	 */
	public void setDay2(Dot day2) {
		this.day2 = day2;
	}
	/**
	 * @return the day3
	 */
	public Dot getDay3() {
		return day3;
	}
	/**
	 * @param day3 the day3 to set
	 */
	public void setDay3(Dot day3) {
		this.day3 = day3;
	}
	/**
	 * @return the day4
	 */
	public Dot getDay4() {
		return day4;
	}
	/**
	 * @param day4 the day4 to set
	 */
	public void setDay4(Dot day4) {
		this.day4 = day4;
	}
	/**
	 * @return the day5
	 */
	public Dot getDay5() {
		return day5;
	}
	/**
	 * @param day5 the day5 to set
	 */
	public void setDay5(Dot day5) {
		this.day5 = day5;
	}
	/**
	 * @return the day6
	 */
	public Dot getDay6() {
		return day6;
	}
	/**
	 * @param day6 the day6 to set
	 */
	public void setDay6(Dot day6) {
		this.day6 = day6;
	}
	/**
	 * @return the day7
	 */
	public Dot getDay7() {
		return day7;
	}
	/**
	 * @param day7 the day7 to set
	 */
	public void setDay7(Dot day7) {
		this.day7 = day7;
	}
	/**
	 * @return the day8
	 */
	public Dot getDay8() {
		return day8;
	}
	/**
	 * @param day8 the day8 to set
	 */
	public void setDay8(Dot day8) {
		this.day8 = day8;
	}
	/**
	 * @return the day9
	 */
	public Dot getDay9() {
		return day9;
	}
	/**
	 * @param day9 the day9 to set
	 */
	public void setDay9(Dot day9) {
		this.day9 = day9;
	}
	/**
	 * @return the day10
	 */
	public Dot getDay10() {
		return day10;
	}
	/**
	 * @param day10 the day10 to set
	 */
	public void setDay10(Dot day10) {
		this.day10 = day10;
	}
	/**
	 * @return the day11
	 */
	public Dot getDay11() {
		return day11;
	}
	/**
	 * @param day11 the day11 to set
	 */
	public void setDay11(Dot day11) {
		this.day11 = day11;
	}
	/**
	 * @return the day12
	 */
	public Dot getDay12() {
		return day12;
	}
	/**
	 * @param day12 the day12 to set
	 */
	public void setDay12(Dot day12) {
		this.day12 = day12;
	}
	/**
	 * @return the day13
	 */
	public Dot getDay13() {
		return day13;
	}
	/**
	 * @param day13 the day13 to set
	 */
	public void setDay13(Dot day13) {
		this.day13 = day13;
	}
	/**
	 * @return the day14
	 */
	public Dot getDay14() {
		return day14;
	}
	/**
	 * @param day14 the day14 to set
	 */
	public void setDay14(Dot day14) {
		this.day14 = day14;
	}
	/**
	 * @return the day15
	 */
	public Dot getDay15() {
		return day15;
	}
	/**
	 * @param day15 the day15 to set
	 */
	public void setDay15(Dot day15) {
		this.day15 = day15;
	}
	/**
	 * @return the day16
	 */
	public Dot getDay16() {
		return day16;
	}
	/**
	 * @param day16 the day16 to set
	 */
	public void setDay16(Dot day16) {
		this.day16 = day16;
	}
	/**
	 * @return the day17
	 */
	public Dot getDay17() {
		return day17;
	}
	/**
	 * @param day17 the day17 to set
	 */
	public void setDay17(Dot day17) {
		this.day17 = day17;
	}
	/**
	 * @return the day18
	 */
	public Dot getDay18() {
		return day18;
	}
	/**
	 * @param day18 the day18 to set
	 */
	public void setDay18(Dot day18) {
		this.day18 = day18;
	}
	/**
	 * @return the day19
	 */
	public Dot getDay19() {
		return day19;
	}
	/**
	 * @param day19 the day19 to set
	 */
	public void setDay19(Dot day19) {
		this.day19 = day19;
	}
	/**
	 * @return the day20
	 */
	public Dot getDay20() {
		return day20;
	}
	/**
	 * @param day20 the day20 to set
	 */
	public void setDay20(Dot day20) {
		this.day20 = day20;
	}
	/**
	 * @return the day21
	 */
	public Dot getDay21() {
		return day21;
	}
	/**
	 * @param day21 the day21 to set
	 */
	public void setDay21(Dot day21) {
		this.day21 = day21;
	}
	/**
	 * @return the day22
	 */
	public Dot getDay22() {
		return day22;
	}
	/**
	 * @param day22 the day22 to set
	 */
	public void setDay22(Dot day22) {
		this.day22 = day22;
	}
	/**
	 * @return the day23
	 */
	public Dot getDay23() {
		return day23;
	}
	/**
	 * @param day23 the day23 to set
	 */
	public void setDay23(Dot day23) {
		this.day23 = day23;
	}
	/**
	 * @return the day24
	 */
	public Dot getDay24() {
		return day24;
	}
	/**
	 * @param day24 the day24 to set
	 */
	public void setDay24(Dot day24) {
		this.day24 = day24;
	}
	/**
	 * @return the day25
	 */
	public Dot getDay25() {
		return day25;
	}
	/**
	 * @param day25 the day25 to set
	 */
	public void setDay25(Dot day25) {
		this.day25 = day25;
	}
	/**
	 * @return the day26
	 */
	public Dot getDay26() {
		return day26;
	}
	/**
	 * @param day26 the day26 to set
	 */
	public void setDay26(Dot day26) {
		this.day26 = day26;
	}
	/**
	 * @return the day27
	 */
	public Dot getDay27() {
		return day27;
	}
	/**
	 * @param day27 the day27 to set
	 */
	public void setDay27(Dot day27) {
		this.day27 = day27;
	}
	/**
	 * @return the day28
	 */
	public Dot getDay28() {
		return day28;
	}
	/**
	 * @param day28 the day28 to set
	 */
	public void setDay28(Dot day28) {
		this.day28 = day28;
	}
	/**
	 * @return the day29
	 */
	public Dot getDay29() {
		return day29;
	}
	/**
	 * @param day29 the day29 to set
	 */
	public void setDay29(Dot day29) {
		this.day29 = day29;
	}
	/**
	 * @return the day30
	 */
	public Dot getDay30() {
		return day30;
	}
	/**
	 * @param day30 the day30 to set
	 */
	public void setDay30(Dot day30) {
		this.day30 = day30;
	}
	/**
	 * @return the day31
	 */
	public Dot getDay31() {
		return day31;
	}
	/**
	 * @param day31 the day31 to set
	 */
	public void setDay31(Dot day31) {
		this.day31 = day31;
	}

	/**
	 * @return the caseDispensing
	 */
	public CaseDispensing_Ke getCaseDispensing() {
		return caseDispensing;
	}

	/**
	 * @param caseDispensing the caseDispensing to set
	 */
	public void setCaseDispensing(CaseDispensing_Ke caseDispensing) {
		this.caseDispensing = caseDispensing;
	}
}
