package org.msh.tb.ua;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.msh.tb.entities.TbCase;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorController;



public abstract class IndicatorVerify extends Indicator2D {
	@In(create=true) IndicatorController indicatorController;
	
	private Map<String,List<ErrItem>> verifyList;
	private boolean counting;
	private boolean overflow;
	
	public class ErrItem {
		public ErrItem(List<TbCase> ltc,String t){
			super();
			this.caseList = ltc;
			this.title = t;
		}
		private List<TbCase> caseList;
		private String title;
		
		public List<TbCase> getCaseList() {
			return caseList;
		}
		public void setCaseList(List<TbCase> caseList) {
			this.caseList = caseList;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
	}

	public boolean isCounting() {
		return counting;
	}

	public void setCounting(boolean counting) {
		this.counting = counting;
	}

	public boolean isOverflow() {
		return overflow;
	}

	public void setOverflow(boolean overflow) {
		this.overflow = overflow;
	}
	
	public Map<String, List<ErrItem>> getVerifyList() {
		return verifyList;
	}
	
	public Map<String, List<ErrItem>> getList() {
		if (verifyList==null)
			if (indicatorController.isExecuting())
				createIndicators();
		return verifyList;
	}

	public void setVerifyList(Map<String, List<ErrItem>> verifyList) {
		this.verifyList = verifyList;
	}

	protected void sortAllLists() {
		if (verifyList!=null)
			for (String key:verifyList.keySet()){
				for (ErrItem it:verifyList.get(key)){
					Collections.sort(it.getCaseList(), new Comparator<TbCase>() {
						  public int compare(TbCase o1, TbCase o2) {
							String name1, name2;
							name1 = o1.getPatient().getFullName();
							name2 = o2.getPatient().getFullName();
						
							if (name1.equals(name2)){
								name2 = name1+"_"+o2.getId();
							}
							Collator myCollator = Collator.getInstance();			    
							return myCollator.compare(name1,name2);
						  }
					});
				}
			}
		
	}
	
	protected void initVerifList(String errmes, int cat1,int cat2,int cat3){
		Map<String, List<ErrItem>>  verifyList = new LinkedHashMap<String,List<ErrItem>>();
		for (int i = 1; i < 4; i++) {
			verifyList.put(getMessage("verify.errorcat"+i),new ArrayList<ErrItem>());
			switch (i){
				case 1:{
					for (int j = 1; j <= cat1; j++) {
						verifyList.get(getMessage("verify.errorcat"+i)).add(new ErrItem(new ArrayList<TbCase>(), getMessage(errmes+i+j)));
					}
					break;
				}
				case 2:{
					for (int j = 1; j <= cat2; j++) {
						verifyList.get(getMessage("verify.errorcat"+i)).add(new ErrItem(new ArrayList<TbCase>(), getMessage(errmes+i+j)));
					}
					break;
				}
				case 3:{
					for (int j = 1; j <= cat3; j++) {
						verifyList.get(getMessage("verify.errorcat"+i)).add(new ErrItem(new ArrayList<TbCase>(), getMessage(errmes+i+j)));
					}
					break;
				}
			}
		}
		setVerifyList(verifyList);
	}
}
