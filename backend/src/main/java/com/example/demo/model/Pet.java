package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Schema(description = "Entidade que representa um pet")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do pet", example = "1")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    @Schema(description = "Nome do pet", example = "Rex", required = true)
    private String nome;

    @NotBlank(message = "Espécie é obrigatória")
    @Column(nullable = false)
    @Schema(description = "Espécie do pet", example = "Cachorro", required = true)
    private String especie;

    @NotBlank(message = "Raça é obrigatória")
    @Column(nullable = false)
    @Schema(description = "Raça do pet", example = "Golden Retriever", required = true)
    private String raca;

    @NotNull(message = "Idade é obrigatória")
    @Positive(message = "Idade deve ser positiva")
    @Column(nullable = false)
    @Schema(description = "Idade do pet em anos", example = "3", required = true)
    private Integer idade;

    @Column(name = "data_cadastro")
    @Schema(description = "Data de cadastro do pet")
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }

    // Constructors
    public Pet() {}

    public Pet(String nome, String especie, String raca, Integer idade) {
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.idade = idade;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
