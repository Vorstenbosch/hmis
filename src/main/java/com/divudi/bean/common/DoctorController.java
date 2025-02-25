/*
 * Open Hospital Management Information System
 *
 * Dr M H B Ariyaratne
 * Acting Consultant (Health Informatics)
 * (94) 71 5812399
 * (94) 71 5812399
 */
package com.divudi.bean.common;

import com.divudi.data.Title;
import com.divudi.entity.Consultant;
import com.divudi.entity.Doctor;
import com.divudi.entity.Person;
import com.divudi.entity.Speciality;
import com.divudi.entity.Vocabulary;
import com.divudi.facade.DoctorFacade;
import com.divudi.facade.PersonFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, MSc, MD(Health Informatics) Acting
 * Consultant (Health Informatics)
 */
@Named
@SessionScoped
public class DoctorController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @Inject
    SpecialityController specialityController;
    @Inject
    CommonController commonController;
    @EJB
    private DoctorFacade ejbFacade;
    @EJB
    private PersonFacade personFacade;
    List<Doctor> selectedItems;
    private Doctor current;
    private List<Doctor> items = null;
    String selectText = "";
    List<Doctor> doctors;
    Speciality speciality;

    public List<Doctor> listDoctors(Speciality speciality) {
        List<Doctor> suggestions;
        String sql;
        sql = " select p "
                + " from Doctor p "
                + " where p.retired=false "
                + " and p.speciality=:spe "
                + " order by p.person.name";
        HashMap hm = new HashMap();
        hm.put("spe", speciality);
        suggestions = getFacade().findByJpql(sql, hm);
        return suggestions;
    }

    public List<Doctor> completeDoctor(String query) {
        List<Doctor> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = " select p from Doctor p "
                    + " where p.retired=false "
                    + " and ((p.person.name) like :q or (p.code) like :q) "
                    + " order by p.person.name";
            HashMap hm = new HashMap();
            hm.put("q", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findByJpql(sql, hm);
        }
        return suggestions;
    }

    public void listDoctors() {
        Date startTime = new Date();

        String temSql;
        temSql = "SELECT d FROM Doctor d where d.retired=false ";
        doctors = getFacade().findByJpql(temSql);

        commonController.printReportDetails(startTime, startTime, startTime, "All doctor Search(/faces/inward/report_all_doctors.xhtml)");

    }

    public List<Doctor> getSelectedItems() {
        String sql = "";
        HashMap hm = new HashMap();
        hm.put("class", Consultant.class);
        if (selectText.trim().equals("")) {
            sql = "select c from Doctor c "
                    + " where c.retired=false "
                    + " and type(c)!=:class "
                    + "order by c.person.name";

        } else {
            sql = "select c from Doctor c "
                    + "where c.retired=false "
                    + " and type(c)!=:class "
                    + " and (c.person.name) like :q "
                    + " order by c.person.name";

            hm.put("q", "%" + getSelectText().toUpperCase() + "%");
        }

        selectedItems = getFacade().findByJpql(sql, hm);

        return selectedItems;
    }

    public void prepareAdd() {
        current = new Doctor();
        specialityController.recreateModel();
    }

    // Method to generate the Excel file and initiate the download
    public void downloadAsExcel() {
        getItems();
        try {
            // Create a new Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Doctor Data");

            // Create a header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Phone");
            headerRow.createCell(2).setCellValue("Fax");
            headerRow.createCell(3).setCellValue("Mobile");
            headerRow.createCell(4).setCellValue("Address");
            headerRow.createCell(5).setCellValue("Code");
            headerRow.createCell(6).setCellValue("Speciality");
            headerRow.createCell(7).setCellValue("Registration");
            headerRow.createCell(8).setCellValue("Qualification");
            headerRow.createCell(9).setCellValue("Refering Charge");

            // Add more columns as needed
            // Populate the data rows
            int rowNum = 1;
            for (Doctor doctor : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(doctor.getName());
                row.createCell(1).setCellValue(doctor.getPerson().getPhone());
                row.createCell(2).setCellValue(doctor.getPerson().getFax());
                row.createCell(3).setCellValue(doctor.getPerson().getMobile());
                row.createCell(4).setCellValue(doctor.getPerson().getAddress());
                row.createCell(5).setCellValue(doctor.getCode());
                row.createCell(6).setCellValue(doctor.getSpeciality().getDescription());
                row.createCell(7).setCellValue(doctor.getRegistration());
                row.createCell(8).setCellValue(doctor.getQualification());
                row.createCell(9).setCellValue(doctor.getCharge());
            }

            // Set the response headers to initiate the download
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"doctor_data.xlsx\"");

            // Write the workbook to the response output stream
            workbook.write(response.getOutputStream());
            workbook.close();
            context.responseComplete();
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
        }
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Deleted Successfully");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        //  getItems();
        current = null;
        getCurrent();
    }

    public void setSelectedItems(List<Doctor> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        items = null;
    }

    public Title[] getTitle() {
        return Title.values();
    }

    public void saveSelected() {
        if (current == null) {
            UtilityController.addErrorMessage("Nothing to save");
            return;
        }
        if (current.getPerson() == null) {
            UtilityController.addErrorMessage("Nothing to save");
            return;
        }
        if (current.getPerson().getName().trim().equals("")) {
            UtilityController.addErrorMessage("Please enter a doctor name");
            return;
        }
        if (current.getSpeciality() == null) {
            UtilityController.addErrorMessage("Please Select Speciality for Doctor");
            return;
        }
        if (current.getPerson().getId() == null || current.getPerson().getId() == 0) {
            getPersonFacade().create(current.getPerson());
        } else {
            getPersonFacade().edit(current.getPerson());
        }
        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Updated Successfully.");
        } else {
            current.setCreatedAt(new Date());
            current.setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            UtilityController.addSuccessMessage("Saved Successfully");
        }
        current = new Doctor();
        recreateModel();
        // getItems();
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public DoctorFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(DoctorFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public DoctorController() {
    }

    public Doctor getCurrent() {
        if (current == null) {
            Person p = new Person();
            current = new Doctor();
            current.setPerson(p);
        }
        return current;
    }

    public void setCurrent(Doctor current) {
        this.current = current;
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
    }

    private DoctorFacade getFacade() {
        return ejbFacade;
    }

    public List<Doctor> getItems() {
        if (items == null) {
            String temSql;
            temSql = "SELECT i FROM Doctor i where i.retired=false ";
            items = getFacade().findByJpql(temSql);
        }
        return items;
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    /**
     *
     */
    @FacesConverter(forClass = Doctor.class)
    public static class DoctorControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DoctorController controller = (DoctorController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "doctorController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Doctor) {
                Doctor o = (Doctor) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + DoctorController.class.getName());
            }
        }
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }

}
