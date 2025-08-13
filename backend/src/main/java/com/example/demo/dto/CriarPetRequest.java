package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Dados para criação de um novo pet")
public class CriarPetRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Schema(description = "Nome do pet", example = "Rex", required = true)
    private String nome;

    @NotBlank(message = "Espécie é obrigatória")
    @Schema(description = "Espécie do pet", example = "Cachorro", required = true)
    private String especie;

    @NotBlank(message = "Raça é obrigatória")
    @Schema(description = "Raça do pet", example = "Golden Retriever", required = true)
    private String raca;

    @NotNull(message = "Idade é obrigatória")
    @Positive(message = "Idade deve ser positiva")
    @Schema(description = "Idade do pet em anos", example = "3", required = true)
    private Integer idade;

    // Constructors
    public CriarPetRequest() {}

    public CriarPetRequest(String nome, String especie, String raca, Integer idade) {
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.idade = idade;
    }

    // Getters and Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }
}
