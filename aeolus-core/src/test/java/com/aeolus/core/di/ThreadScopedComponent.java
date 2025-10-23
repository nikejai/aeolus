package com.aeolus.core.di;

import com.aeolus.core.di.annotations.Component;
import com.aeolus.core.di.annotations.Scope;

@Component
@Scope("thread")
class ThreadScopedComponent { }
