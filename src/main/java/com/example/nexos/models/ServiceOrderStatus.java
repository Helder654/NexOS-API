package com.example.nexos.models;

public enum ServiceOrderStatus {
    ABERTA,
    EM_ANALISE,
    AGUARDANDO_APROVACAO,
    EM_REPARO,
    FINALIZADA,
    CANCELADA;

    public boolean canTransitionTo(ServiceOrderStatus newStatus) {
        return switch (this) {
            case ABERTA -> newStatus == EM_ANALISE || newStatus == CANCELADA;
            case EM_ANALISE -> newStatus == AGUARDANDO_APROVACAO || newStatus == CANCELADA;
            case AGUARDANDO_APROVACAO -> newStatus == EM_REPARO || newStatus == CANCELADA;
            case EM_REPARO -> newStatus == FINALIZADA || newStatus == CANCELADA;
            case FINALIZADA, CANCELADA -> false;
        };
    }
}
