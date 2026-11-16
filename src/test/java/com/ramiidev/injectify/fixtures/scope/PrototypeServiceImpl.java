package com.ramiidev.injectify.fixtures.scope;

import com.ramiidev.injectify.core.beans.definition.BeanScope;
import com.ramiidev.injectify.shared.annotation.Scope;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
@Scope(BeanScope.PROTOTYPE)
public class PrototypeServiceImpl implements PrototypeService{
}
