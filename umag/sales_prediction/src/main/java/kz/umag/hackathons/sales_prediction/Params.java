package kz.umag.hackathons.sales_prediction;

import com.beust.jcommander.Parameter;

/**
 * Parameters of the application.
 */
public class Params {

  @Parameter(
      names = "--small",
      description = "Small data mode."
  )
  private boolean small = false;
}
