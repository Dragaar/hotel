package com.rosivanyshyn.controller.other.booking;

import com.rosivanyshyn.controller.dispatcher.Controller;
import com.rosivanyshyn.controller.dispatcher.viewresolve.ViewResolver;
import com.rosivanyshyn.db.dao.constant.AccountRole;
import com.rosivanyshyn.db.dao.entity.Booking;

import com.rosivanyshyn.exeption.AppException;
import com.rosivanyshyn.service.BookingService;

import com.rosivanyshyn.service.implMySQL.BookingServiceImpl;

import com.rosivanyshyn.utils.MySQLQueryBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;

import java.util.ArrayList;

import static com.rosivanyshyn.controller.dispatcher.ControllerConstant.ALL_BOOKINGS_JSP;

public class GetAllBookingController implements Controller {

    BookingService bookingService = new BookingServiceImpl();
    int pageId, recordsPerPage, currentRecord;
    @Override
    public ViewResolver resolve(HttpServletRequest request, HttpServletResponse response) {
        ViewResolver resolver = new ViewResolver();

        try {
            HttpSession session = request.getSession(false);
            @NonNull final AccountRole role = (AccountRole) session.getAttribute("role");

            if(role.equals(AccountRole.MANAGER)) {
                ArrayList<Booking> bookings;

                getPageConfig(request);

                MySQLQueryBuilder queryBuilder = new MySQLQueryBuilder();
                queryBuilder.setLabel("booking");
                queryBuilder.limit(currentRecord, recordsPerPage);
                bookings = bookingService.findFewOrdersAndSort(queryBuilder.getQuery());

                //orders = orderService.findFewOrders(currentRecord, recordsPerPage);
                request.setAttribute("bookings", bookings);

                request.setAttribute("page", pageId);
                //for pagination links
                request.setAttribute("currentController", "getAllBookings");

                //totalRecordCount should work with search, pagination of the table and user filters
                //int totalRecordCount = apartmentService.getRecordCount();
                //int totalPagesCount = (int) Math.ceil(totalRecordCount * 1.0 / recordsPerPage);
                //request.setAttribute("totalPagesCount", apartments);

                resolver.forward(ALL_BOOKINGS_JSP);
            }
        } catch (RuntimeException ex){
            throw new AppException("Cannot get All Bookings", ex);
        }
        return resolver;
    }

    private void getPageConfig(HttpServletRequest request) {
        String reqPageId = request.getParameter("page");
        String reqRecordsPerPage = request.getParameter("recordsPerPage");

        if(reqPageId==null ){
            pageId = 1;
            currentRecord=1;
        } else {
            pageId = Integer.parseInt(reqPageId);
            currentRecord = pageId;
        }

        if(reqRecordsPerPage==null){
            recordsPerPage = 8;
        } else {
            recordsPerPage = Integer.parseInt(reqRecordsPerPage);
        }

        if(pageId>1)
        {
            int temp = pageId-1;
            currentRecord = temp*recordsPerPage+1;
        }
    }
}

