package br.com.caredrop.util;

import java.util.Set;

public final class Validacoes {
    private static final Set<String> TIPOS = Set.of("A+","A-","B+","B-","AB+","AB-","O+","O-");
    private Validacoes() {}
    public static String cpf(String entrada) {
        String cpf=entrada==null?"":entrada.replaceAll("\\D","");
        if(cpf.length()!=11||cpf.chars().distinct().count()==1)throw new IllegalArgumentException("Informe um CPF válido com 11 dígitos.");
        int soma=0;for(int i=0;i<9;i++)soma+=(cpf.charAt(i)-'0')*(10-i);int d1=11-soma%11;if(d1>9)d1=0;
        soma=0;for(int i=0;i<10;i++)soma+=(cpf.charAt(i)-'0')*(11-i);int d2=11-soma%11;if(d2>9)d2=0;
        if(d1!=cpf.charAt(9)-'0'||d2!=cpf.charAt(10)-'0')throw new IllegalArgumentException("Informe um CPF válido.");
        return cpf;
    }
    public static void tipoSanguineo(String tipo) { if(!TIPOS.contains(tipo))throw new IllegalArgumentException("Tipo sanguíneo inválido."); }
    public static void quantidadePositiva(int quantidade) { if(quantidade<=0)throw new IllegalArgumentException("A quantidade deve ser maior que zero."); }
}
