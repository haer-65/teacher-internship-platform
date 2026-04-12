package com.teacher.internship.modules.application.vo;

import java.util.ArrayList;
import java.util.List;

public class AssignmentOptionVO {

    private List<OptionItemVO> internshipBases = new ArrayList<>();
    private List<OptionItemVO> innerTeachers = new ArrayList<>();
    private List<OptionItemVO> baseTeachers = new ArrayList<>();

    public List<OptionItemVO> getInternshipBases() {
        return internshipBases;
    }

    public void setInternshipBases(List<OptionItemVO> internshipBases) {
        this.internshipBases = internshipBases;
    }

    public List<OptionItemVO> getInnerTeachers() {
        return innerTeachers;
    }

    public void setInnerTeachers(List<OptionItemVO> innerTeachers) {
        this.innerTeachers = innerTeachers;
    }

    public List<OptionItemVO> getBaseTeachers() {
        return baseTeachers;
    }

    public void setBaseTeachers(List<OptionItemVO> baseTeachers) {
        this.baseTeachers = baseTeachers;
    }
}
