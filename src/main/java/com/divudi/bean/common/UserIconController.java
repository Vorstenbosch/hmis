/*
 * Open Hospital Management Information System
 *
 * Dr M H B Ariyaratne
 * Consultant (Health Informatics)
 * (94) 71 5812399
 * (94) 71 5812399
 */
package com.divudi.bean.common;

import com.divudi.data.Icon;
import com.divudi.entity.UserIcon;
import com.divudi.entity.WebUser;
import com.divudi.facade.UserIconFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, MSc, MD(Health Informatics) Acting
 * Consultant (Health Informatics)
 */
@Named
@SessionScoped
public class UserIconController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private UserIconFacade ejbFacade;
    private UserIcon current;
    private List<UserIcon> userIcons = null;
    private List<Icon> icons;
    private WebUser user;
    private Icon icon;

// Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public void addUserIcon() {
        if (icon == null) {
            JsfUtil.addErrorMessage("Select Icon");
            return;
        }
        if (user == null) {
            JsfUtil.addErrorMessage("Program Error. Cannot have this page without a user. Create an issue in GitHub");
            return;
        }
        double newOrder = getUserIcons().size() + 1;
        UserIcon existingUI = findUserIconByOrder(newOrder);

        if (existingUI == null) {
            UserIcon ui = new UserIcon();
            ui.setWebUser(user);
            ui.setIcon(icon);
            ui.setOrderNumber(newOrder);
            save(ui);
            getUserIcons().add(ui);
            reOrderUserIcons();
        } else {
            JsfUtil.addErrorMessage("Icon already exists at this position");
        }
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public void moveSelectedUserIconUp() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return;
        }
        double currentOrder = current.getOrderNumber();
        UserIcon prevIcon = findUserIconByOrder(currentOrder - 1);
        if (prevIcon != null) {
            prevIcon.setOrderNumber(currentOrder);
            current.setOrderNumber(currentOrder - 1);
            save(prevIcon);
            save(current);
        } else {
            JsfUtil.addErrorMessage("Already at the top");
        }
        fillUserIcons();
    }

    public void moveSelectedUserIconDown() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return;
        }
        double currentOrder = current.getOrderNumber();
        UserIcon nextIcon = findUserIconByOrder(currentOrder + 1);
        if (nextIcon != null) {
            nextIcon.setOrderNumber(currentOrder);
            current.setOrderNumber(currentOrder + 1);
            save(nextIcon);
            save(current);
        } else {
            JsfUtil.addErrorMessage("Already at the bottom");
        }
        fillUserIcons();
    }

    private UserIcon findUserIconByOrder(double order) {
        for (UserIcon ui : userIcons) {
            if (ui.getOrderNumber() == order) {
                return ui;
            }
        }
        return null;
    }

    // Method to re-order UserIcons based on orderNumber
    public void reOrderUserIcons() {
        if (userIcons == null || userIcons.isEmpty()) {
            return;
        }
        double order = 0.0;
        for (UserIcon ui : userIcons) {
            ui.setOrderNumber(order++);
            save(ui);
        }
    }

    // Method to validate if the Icon is already added for the user
    public boolean isIconAlreadyAdded() {
        for (UserIcon ui : userIcons) {
            if (ui.getIcon() == icon) {
                JsfUtil.addErrorMessage("Icon already added");
                return true;
            }
        }
        return false;
    }

    private void fillUserIcons() {
        userIcons = fillUserIcons(user);
    }

    public List<UserIcon> fillUserIcons(WebUser u) {
        List<UserIcon> uis = null;
        if (u == null) {
            userIcons = null;
            return uis;
        }
        String Jpql = "select i "
                + " from UserIcon i "
                + " where i.retired=:ret "
                + " and i.webUser=:u ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("u", u);
        uis = getFacade().findByJpql(Jpql, m);
        Collections.sort(uis, new UserIconOrderComparator());
        return uis;
    }

    
    public void save(UserIcon ui) {
        if (ui == null) {
            return;
        }
        if (ui.getId() != null) {
            getFacade().edit(ui);
        } else {
            getFacade().create(ui);
        }
    }

    private UserIconFacade getEjbFacade() {
        return ejbFacade;
    }

    public UserIconController() {
    }

    public UserIcon getCurrent() {
        if (current == null) {
            current = new UserIcon();
        }
        return current;
    }

    public void setCurrent(UserIcon current) {
        this.current = current;
        fillUserIcons();
    }

    public void removeUserIcon() {
        if (current != null) {
            current.setRetired(true);
            save(current);
            UtilityController.addSuccessMessage("Removed Successfully");
        } else {
            UtilityController.addSuccessMessage("Nothing to Remove");
        }
    }

    private UserIconFacade getFacade() {
        return ejbFacade;
    }

    public List<Icon> getIcons() {
        if (icons == null) {
            icons = Arrays.asList(Icon.values());
        }
        return icons;
    }

    public WebUser getUser() {
        return user;
    }

    public void setUser(WebUser user) {
        this.user = user;
        userIcons = fillUserIcons(user);
    }

    public List<UserIcon> getUserIcons() {
        if (userIcons == null) {
            userIcons = new ArrayList<>();
        }
        return userIcons;
    }

    public void setUserIcons(List<UserIcon> userIcons) {
        this.userIcons = userIcons;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     *
     */
    @FacesConverter(forClass = UserIcon.class)
    public static class UserIconConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserIconController controller = (UserIconController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "userIconConverter");
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
            if (object instanceof UserIcon) {
                UserIcon o = (UserIcon) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + UserIconController.class.getName());
            }
        }
    }

    public static class UserIconOrderComparator implements Comparator<UserIcon> {

        @Override
        public int compare(UserIcon o1, UserIcon o2) {
            return Double.compare(o1.getOrderNumber(), o2.getOrderNumber());
        }
    }

}
