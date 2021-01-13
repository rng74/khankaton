package kz.umag.hackathons.sales_prediction.impl;

import kz.umag.hackathons.sales_prediction.api.SalesDataHandler;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Bindings for sales database related implementations.
 */
public final class SalesDataModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(SalesDataHandler.class).to(SalesDataLocalHandler.class).in(Scopes.SINGLETON);
  }
}
