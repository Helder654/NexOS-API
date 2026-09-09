package com.example.nexos.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.example.nexos.dtos.ServiceOrderStatusHistoryDTO;
import com.example.nexos.models.ServiceOrderModel;
import com.example.nexos.models.ServiceOrderStatus;
import com.example.nexos.models.ServiceOrderStatusHistoryModel;

@Component
public class ServiceOrderStatusHistoryMapper {

    public ServiceOrderStatusHistoryModel map(ServiceOrderModel serviceOrderModel, ServiceOrderStatus statusAnterior,
            ServiceOrderStatus statusNovo) {
        ServiceOrderStatusHistoryModel serviceOrderStatusHistoryModel = new ServiceOrderStatusHistoryModel();

        serviceOrderStatusHistoryModel.setServiceOrder(serviceOrderModel);
        serviceOrderStatusHistoryModel.setStatusAnterior(statusAnterior);
        serviceOrderStatusHistoryModel.setStatusNovo(statusNovo);
        serviceOrderStatusHistoryModel.setDataAlteracao(LocalDateTime.now());

        return serviceOrderStatusHistoryModel;
    }

    public ServiceOrderStatusHistoryDTO map(ServiceOrderStatusHistoryModel serviceOrderStatusHistoryModel) {
        ServiceOrderStatusHistoryDTO serviceOrderStatusHistoryDTO = new ServiceOrderStatusHistoryDTO();

        serviceOrderStatusHistoryDTO.setId(serviceOrderStatusHistoryModel.getId());
        serviceOrderStatusHistoryDTO.setServiceOrderId(serviceOrderStatusHistoryModel.getServiceOrder().getId());
        serviceOrderStatusHistoryDTO.setStatusAnterior(serviceOrderStatusHistoryModel.getStatusAnterior());
        serviceOrderStatusHistoryDTO.setStatusNovo(serviceOrderStatusHistoryModel.getStatusNovo());
        serviceOrderStatusHistoryDTO.setDataAlteracao(serviceOrderStatusHistoryModel.getDataAlteracao());

        return serviceOrderStatusHistoryDTO;
    }

}
