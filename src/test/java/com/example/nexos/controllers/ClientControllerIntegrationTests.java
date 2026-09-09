package com.example.nexos.controllers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.nexos.models.ClientModel;
import com.example.nexos.repositories.ClientRepository;

@SpringBootTest
class ClientControllerIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ClientRepository clientRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        clientRepository.deleteAll();
    }

    @Test
    void shouldCreateClient() throws Exception {
        mockMvc.perform(post("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Ana Souza",
                          "telefone": "(11) 99999-9999",
                          "email": "ana.souza@example.com"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.telefone").value("(11) 99999-9999"))
                .andExpect(jsonPath("$.email").value("ana.souza@example.com"));
    }

    @Test
    void shouldRejectInvalidClient() throws Exception {
        mockMvc.perform(post("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "",
                          "telefone": "",
                          "email": "email-invalido"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnClientById() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));

        mockMvc.perform(get("/clients/{id}", clientModel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientModel.getId()))
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.telefone").value("(11) 99999-9999"))
                .andExpect(jsonPath("$.email").value("ana.souza@example.com"));
    }

    @Test
    void shouldReturnNotFoundWhenClientDoesNotExist() throws Exception {
        mockMvc.perform(get("/clients/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detail").value("Cliente com id 999 não foi encontrado"));
    }

    @Test
    void shouldReturnEmptyClientList() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldReturnAllClients() throws Exception {
        clientRepository.saveAll(List.of(
                new ClientModel(null, "Ana Souza", "(11) 99999-9999", "ana.souza@example.com"),
                new ClientModel(null, "Bruno Lima", "(11) 98888-8888", "bruno.lima@example.com")));

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldReturnPaginatedAndSortedClients() throws Exception {
        clientRepository.saveAll(List.of(
                new ClientModel(null, "Carlos Lima", "(11) 97777-7777", "carlos.lima@example.com"),
                new ClientModel(null, "Ana Souza", "(11) 99999-9999", "ana.souza@example.com"),
                new ClientModel(null, "Bruno Lima", "(11) 98888-8888", "bruno.lima@example.com")));

        mockMvc.perform(get("/clients?page=1&size=1&sort=nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Bruno Lima"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldUpdateClient() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));

        mockMvc.perform(put("/clients/{id}", clientModel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Ana Silva",
                          "telefone": "(11) 98888-8888",
                          "email": "ana.silva@example.com"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientModel.getId()))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.telefone").value("(11) 98888-8888"))
                .andExpect(jsonPath("$.email").value("ana.silva@example.com"));
    }

    @Test
    void shouldRejectInvalidClientUpdate() throws Exception {
        mockMvc.perform(put("/clients/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "",
                          "telefone": "",
                          "email": "email-invalido"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentClient() throws Exception {
        mockMvc.perform(put("/clients/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Ana Silva",
                          "telefone": "(11) 98888-8888",
                          "email": "ana.silva@example.com"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Cliente com id 999 não foi encontrado"));
    }

    @Test
    void shouldDeleteClient() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));

        mockMvc.perform(delete("/clients/{id}", clientModel.getId()))
                .andExpect(status().isNoContent());

        assertTrue(clientRepository.findById(clientModel.getId()).isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonexistentClient() throws Exception {
        mockMvc.perform(delete("/clients/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Cliente com id 999 não foi encontrado"));
    }

}
