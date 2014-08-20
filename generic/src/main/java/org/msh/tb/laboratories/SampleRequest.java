package org.msh.tb.laboratories;


import org.msh.tb.entities.*;
import org.msh.utils.date.LocaleDateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Temporary information about samples requests
 * @author Ricardo Memoria
 *
 */
public class SampleRequest {

	private int index;

    private String sampleNumber;

    private Date dateCollected;
	
	private boolean addExamCulture;
	private boolean addExamMicroscopy;
	private boolean addExamDST;
	private boolean addExamIdentification;

    /**
     * Store list of laboratory exams used in the {@link SamplesRequestList}
     */
    private List<ExamMicroscopy> examsMicroscopy;
    private List<ExamCulture> examsCulture;
    private List<ExamDST> examsDST;
    private List<ExamXpert> examsXpert;

    /**
     * Return the date collected to be displayed to the user
     * @return
     */
    public String getDisplayDateCollected() {
        return dateCollected != null? LocaleDateConverter.getDisplayDate(dateCollected, false): "";
    }



	/**
	 * @return the addExamCulture
	 */
	public boolean isAddExamCulture() {
		return addExamCulture;
	}

	/**
	 * @param addExamCulture the addExamCulture to set
	 */
	public void setAddExamCulture(boolean addExamCulture) {
		this.addExamCulture = addExamCulture;
	}

	/**
	 * @return the addExamMicroscopy
	 */
	public boolean isAddExamMicroscopy() {
		return addExamMicroscopy;
	}

	/**
	 * @param addExamMicroscopy the addExamMicroscopy to set
	 */
	public void setAddExamMicroscopy(boolean addExamMicroscopy) {
		this.addExamMicroscopy = addExamMicroscopy;
	}

	/**
	 * @return the addExamDST
	 */
	public boolean isAddExamDST() {
		return addExamDST;
	}

	/**
	 * @param addExamDST the addExamDST to set
	 */
	public void setAddExamDST(boolean addExamDST) {
		this.addExamDST = addExamDST;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the addExamIdentification
	 */
	public boolean isAddExamIdentification() {
		return addExamIdentification;
	}

	/**
	 * @param addExamIdentification the addExamIdentification to set
	 */
	public void setAddExamIdentification(boolean addExamIdentification) {
		this.addExamIdentification = addExamIdentification;
	}

    public String getSampleNumber() {
        return sampleNumber;
    }

    public void setSampleNumber(String sampleNumber) {
        this.sampleNumber = sampleNumber;
    }

    public Date getDateCollected() {
        return dateCollected;
    }

    public void setDateCollected(Date dateCollected) {
        this.dateCollected = dateCollected;
    }

    public List<ExamMicroscopy> getExamsMicroscopy() {
        if (examsMicroscopy == null) {
            examsMicroscopy = new ArrayList<ExamMicroscopy>();
        }
        return examsMicroscopy;
    }

    public void setExamsMicroscopy(List<ExamMicroscopy> examsMicroscopy) {
        this.examsMicroscopy = examsMicroscopy;
    }

    public List<ExamCulture> getExamsCulture() {
        if (examsCulture == null) {
            examsCulture = new ArrayList<ExamCulture>();
        }
        return examsCulture;
    }

    public void setExamsCulture(List<ExamCulture> examsCulture) {
        this.examsCulture = examsCulture;
    }

    public List<ExamDST> getExamsDST() {
        if (examsDST == null) {
            examsDST = new ArrayList<ExamDST>();
        }
        return examsDST;
    }

    public void setExamsDST(List<ExamDST> examsDST) {
        this.examsDST = examsDST;
    }

    public List<ExamXpert> getExamsXpert() {
        if (examsXpert == null) {
            examsXpert = new ArrayList<ExamXpert>();
        }
        return examsXpert;
    }

    public void setExamsXpert(List<ExamXpert> examsXpert) {
        this.examsXpert = examsXpert;
    }
}
