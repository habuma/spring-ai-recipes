package com.example.chatloop;

public class FactCheckingConstants {

  public static final String FACT_CHECKING_PROMPT_TEMPLATE = """
      Determine whether the claim is supported by the provided context.
      
      A claim is supported only if it is:
      - explicitly stated in the context, or
      - necessarily implied by the context.
      
      Do not consider a claim supported merely because it is plausible or
      consistent with the context.
      
      Do not combine separate facts from the context to infer a relationship
      that the context does not establish.
      
      Context:
      {document}
      
      Claim:
      {claim}
      
      Respond with:
      - YES if the claim is supported by the context.
      - NO if the claim is not supported by the context.
      
      Respond with only "YES" or "NO".
      """;

}
