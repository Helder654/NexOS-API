package com.example.nexos.mappers;

import org.springframework.stereotype.Component;

import com.example.nexos.dtos.CreateServiceOrderDTO;
import com.example.nexos.dtos.ServiceOrderDTO;
import com.example.nexos.dtos.TechnicianSummaryDTO;
import com.example.nexos.dtos.UpdateServiceOrderDTO;
import com.example.nexos.models.ClientModel;
import com.example.nexos.models.ServiceOrderModel;

@Component
public class ServiceOrderMapper {

    public ServiceOrderModel map(CreateServiceOrderDTO createServiceOrderDTO, ClientModel clientModel) {
        ServiceOrderModel serviceOrderModel = new ServiceOrderModel();

        serviceOrderModel.setCliente(clientModel);
        serviceOrderModel.setConsole(createServiceOrderDTO.getConsole());
        serviceOrderModel.setDefeitoRelatado(createServiceOrderDTO.getDefeitoRelatado());
        serviceOrderModel.setAnaliseTecnico(createServiceOrderDTO.getAnaliseTecnico());
        serviceOrderModel.setDiagnostico(createServiceOrderDTO.getDiagnostico());
        serviceOrderModel.setValor(createServiceOrderDTO.getValor());
        serviceOrderModel.setCustoReparo(createServiceOrderDTO.getCustoReparo());

        return serviceOrderModel;
    }

    public ServiceOrderDTO map(ServiceOrderModel serviceOrderModel) {
        ServiceOrderDTO serviceOrderDTO = new ServiceOrderDTO();

        serviceOrderDTO.setId(serviceOrderModel.getId());
        serviceOrderDTO.setClienteId(serviceOrderModel.getCliente().getId());
        serviceOrderDTO.setTecnico(mapTechnician(serviceOrderModel));
        serviceOrderDTO.setConsole(serviceOrderModel.getConsole());
        serviceOrderDTO.setDefeitoRelatado(serviceOrderModel.getDefeitoRelatado());
        serviceOrderDTO.setAnaliseTecnico(serviceOrderModel.getAnaliseTecnico());
        serviceOrderDTO.setDataAbertura(serviceOrderModel.getDataAbertura());
        serviceOrderDTO.setDiagnostico(serviceOrderModel.getDiagnostico());
        serviceOrderDTO.setValor(serviceOrderModel.getValor());
        serviceOrderDTO.setCustoReparo(serviceOrderModel.getCustoReparo());
        serviceOrderDTO.setStatus(serviceOrderModel.getStatus());

        return serviceOrderDTO;
    }

    public void updateModel(UpdateServiceOrderDTO updateServiceOrderDTO, ServiceOrderModel serviceOrderModel) {
        serviceOrderModel.setConsole(updateServiceOrderDTO.getConsole());
        serviceOrderModel.setDefeitoRelatado(updateServiceOrderDTO.getDefeitoRelatado());
        serviceOrderModel.setAnaliseTecnico(updateServiceOrderDTO.getAnaliseTecnico());
        serviceOrderModel.setDiagnostico(updateServiceOrderDTO.getDiagnostico());
        serviceOrderModel.setValor(updateServiceOrderDTO.getValor());
        serviceOrderModel.setCustoReparo(updateServiceOrderDTO.getCustoReparo());
    }

    private TechnicianSummaryDTO mapTechnician(ServiceOrderModel serviceOrderModel) {
        if (serviceOrderModel.getTecnico() == null) {
            return null;
        }

        TechnicianSummaryDTO technicianSummaryDTO = new TechnicianSummaryDTO();
        technicianSummaryDTO.setId(serviceOrderModel.getTecnico().getId());
        technicianSummaryDTO.setNome(serviceOrderModel.getTecnico().getNome());
        technicianSummaryDTO.setEmail(serviceOrderModel.getTecnico().getEmail());

        return technicianSummaryDTO;
    }

}
